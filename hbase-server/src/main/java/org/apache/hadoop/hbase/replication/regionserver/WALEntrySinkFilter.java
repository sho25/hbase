begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|replication
operator|.
name|regionserver
package|;
end_package

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
name|TableName
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
name|AsyncConnection
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceStability
import|;
end_import

begin_comment
comment|/**  * Implementations are installed on a Replication Sink called from inside  * ReplicationSink#replicateEntries to filter replicated WALEntries based off WALEntry attributes.  * Currently only table name and replication write time are exposed (WALEntry is a private,  * internal class so we cannot pass it here). To install, set  *<code>hbase.replication.sink.walentryfilter</code> to the name of the implementing  * class. Implementing class must have a no-param Constructor.  *<p>This filter is of limited use. It is better to filter on the replication source rather than  * here after the edits have been shipped on the replication sink. That said, applications such  * as the hbase-indexer want to filter out any edits that were made before replication was enabled.  * @see org.apache.hadoop.hbase.replication.WALEntryFilter for filtering on the replication  * source-side.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|REPLICATION
argument_list|)
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
interface|interface
name|WALEntrySinkFilter
block|{
comment|/**    * Name of configuration to set with name of implementing WALEntrySinkFilter class.    */
specifier|public
specifier|static
specifier|final
name|String
name|WAL_ENTRY_FILTER_KEY
init|=
literal|"hbase.replication.sink.walentrysinkfilter"
decl_stmt|;
comment|/**    * Called after Construction.    * Use passed Connection to keep any context the filter might need.    */
name|void
name|init
parameter_list|(
name|AsyncConnection
name|conn
parameter_list|)
function_decl|;
comment|/**    * @param table Table edit is destined for.    * @param writeTime Time at which the edit was created on the source.    * @return True if we are to filter out the edit.    */
name|boolean
name|filter
parameter_list|(
name|TableName
name|table
parameter_list|,
name|long
name|writeTime
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

