begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|coprocessor
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|CellScanner
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|Coprocessor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|HBaseInterfaceAudience
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|MetaMutationAnnotation
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|classification
operator|.
name|InterfaceStability
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|client
operator|.
name|Mutation
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|AdminProtos
operator|.
name|WALEntry
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|regionserver
operator|.
name|Region
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|replication
operator|.
name|ReplicationEndpoint
import|;
end_import

begin_comment
comment|/**  * Defines coprocessor hooks for interacting with operations on the  * {@link org.apache.hadoop.hbase.regionserver.HRegionServer} process.  *  * Since most implementations will be interested in only a subset of hooks, this class uses  * 'default' functions to avoid having to add unnecessary overrides. When the functions are  * non-empty, it's simply to satisfy the compiler by returning value of expected (non-void) type.  * It is done in a way that these default definitions act as no-op. So our suggestion to  * implementation would be to not call these 'default' methods from overrides.  *<br><br>  *  *<h3>Exception Handling</h3>  * For all functions, exception handling is done as follows:  *<ul>  *<li>Exceptions of type {@link IOException} are reported back to client.</li>  *<li>For any other kind of exception:  *<ul>  *<li>If the configuration {@link CoprocessorHost#ABORT_ON_ERROR_KEY} is set to true, then  *         the server aborts.</li>  *<li>Otherwise, coprocessor is removed from the server and  *         {@link org.apache.hadoop.hbase.DoNotRetryIOException} is returned to the client.</li>  *</ul>  *</li>  *</ul>  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|COPROC
argument_list|)
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
interface|interface
name|RegionServerObserver
extends|extends
name|Coprocessor
block|{
comment|/**    * Called before stopping region server.    * @param ctx the environment to interact with the framework and region server.    */
specifier|default
name|void
name|preStopRegionServer
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionServerCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|)
throws|throws
name|IOException
block|{}
comment|/**    * Called before the regions merge.    * Call {@link org.apache.hadoop.hbase.coprocessor.ObserverContext#bypass()} to skip the merge.    * @param ctx the environment to interact with the framework and region server.    * @param regionA region being merged.    * @param regionB region being merged.    */
specifier|default
name|void
name|preMerge
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionServerCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
specifier|final
name|Region
name|regionA
parameter_list|,
specifier|final
name|Region
name|regionB
parameter_list|)
throws|throws
name|IOException
block|{}
comment|/**    * called after the regions merge.    * @param ctx the environment to interact with the framework and region server.    * @param regionA region being merged.    * @param regionB region being merged.    */
specifier|default
name|void
name|postMerge
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionServerCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
specifier|final
name|Region
name|regionA
parameter_list|,
specifier|final
name|Region
name|regionB
parameter_list|,
specifier|final
name|Region
name|mergedRegion
parameter_list|)
throws|throws
name|IOException
block|{}
comment|/**    * This will be called before PONR step as part of regions merge transaction. Calling    * {@link org.apache.hadoop.hbase.coprocessor.ObserverContext#bypass()} rollback the merge    * @param ctx the environment to interact with the framework and region server.    * @param regionA region being merged.    * @param regionB region being merged.    * @param metaEntries mutations to execute on hbase:meta atomically with regions merge updates.    *        Any puts or deletes to execute on hbase:meta can be added to the mutations.    */
specifier|default
name|void
name|preMergeCommit
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionServerCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
specifier|final
name|Region
name|regionA
parameter_list|,
specifier|final
name|Region
name|regionB
parameter_list|,
annotation|@
name|MetaMutationAnnotation
name|List
argument_list|<
name|Mutation
argument_list|>
name|metaEntries
parameter_list|)
throws|throws
name|IOException
block|{}
comment|/**    * This will be called after PONR step as part of regions merge transaction.    * @param ctx the environment to interact with the framework and region server.    * @param regionA region being merged.    * @param regionB region being merged.    */
specifier|default
name|void
name|postMergeCommit
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionServerCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
specifier|final
name|Region
name|regionA
parameter_list|,
specifier|final
name|Region
name|regionB
parameter_list|,
specifier|final
name|Region
name|mergedRegion
parameter_list|)
throws|throws
name|IOException
block|{}
comment|/**    * This will be called before the roll back of the regions merge.    * @param ctx the environment to interact with the framework and region server.    * @param regionA region being merged.    * @param regionB region being merged.    */
specifier|default
name|void
name|preRollBackMerge
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionServerCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
specifier|final
name|Region
name|regionA
parameter_list|,
specifier|final
name|Region
name|regionB
parameter_list|)
throws|throws
name|IOException
block|{}
comment|/**    * This will be called after the roll back of the regions merge.    * @param ctx the environment to interact with the framework and region server.    * @param regionA region being merged.    * @param regionB region being merged.    */
specifier|default
name|void
name|postRollBackMerge
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionServerCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
specifier|final
name|Region
name|regionA
parameter_list|,
specifier|final
name|Region
name|regionB
parameter_list|)
throws|throws
name|IOException
block|{}
comment|/**    * This will be called before executing user request to roll a region server WAL.    * @param ctx the environment to interact with the framework and region server.    */
specifier|default
name|void
name|preRollWALWriterRequest
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionServerCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|)
throws|throws
name|IOException
block|{}
comment|/**    * This will be called after executing user request to roll a region server WAL.    * @param ctx the environment to interact with the framework and region server.    */
specifier|default
name|void
name|postRollWALWriterRequest
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionServerCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|)
throws|throws
name|IOException
block|{}
comment|/**    * This will be called after the replication endpoint is instantiated.    * @param ctx the environment to interact with the framework and region server.    * @param endpoint - the base endpoint for replication    * @return the endpoint to use during replication.    */
specifier|default
name|ReplicationEndpoint
name|postCreateReplicationEndPoint
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionServerCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
name|ReplicationEndpoint
name|endpoint
parameter_list|)
block|{
return|return
name|endpoint
return|;
block|}
comment|/**    * This will be called before executing replication request to shipping log entries.    * @param ctx the environment to interact with the framework and region server.    * @param entries list of WALEntries to replicate    * @param cells Cells that the WALEntries refer to (if cells is non-null)    */
specifier|default
name|void
name|preReplicateLogEntries
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionServerCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
name|List
argument_list|<
name|WALEntry
argument_list|>
name|entries
parameter_list|,
name|CellScanner
name|cells
parameter_list|)
throws|throws
name|IOException
block|{}
comment|/**    * This will be called after executing replication request to shipping log entries.    * @param ctx the environment to interact with the framework and region server.    * @param entries list of WALEntries to replicate    * @param cells Cells that the WALEntries refer to (if cells is non-null)    */
specifier|default
name|void
name|postReplicateLogEntries
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionServerCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
name|List
argument_list|<
name|WALEntry
argument_list|>
name|entries
parameter_list|,
name|CellScanner
name|cells
parameter_list|)
throws|throws
name|IOException
block|{}
block|}
end_interface

end_unit

