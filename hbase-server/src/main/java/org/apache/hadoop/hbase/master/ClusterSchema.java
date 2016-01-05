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
name|master
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
name|io
operator|.
name|InterruptedIOException
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
name|NamespaceDescriptor
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

begin_comment
comment|/**  * View and edit the current cluster schema. Use this API making any modification to  * namespaces, tables, etc.  *  *<h2>Implementation Notes</h2>  * Nonces are for when operation is non-idempotent to ensure once-only semantic, even  * across process failures.  */
end_comment

begin_comment
comment|// ClusterSchema is introduced to encapsulate schema modification. Currently the different aspects
end_comment

begin_comment
comment|// are spread about the code base. This effort is about cleanup, shutting down access, and
end_comment

begin_comment
comment|// coalescing common code. In particular, we'd contain filesystem modification. Other
end_comment

begin_comment
comment|// benefits are to make all schema modification work the same way (one way to do an operation only
end_comment

begin_comment
comment|// rather than the current approach where how an operation is done varies with context) and to make
end_comment

begin_comment
comment|// it so clusterschema modification can stand apart from Master to faciliate standalone
end_comment

begin_comment
comment|// testing. It is part of the filesystem refactor project that undoes the dependency on a
end_comment

begin_comment
comment|// layout in HDFS that mimics our model of tables have regions have column families have files.
end_comment

begin_comment
comment|// With this Interface in place, with all modifications going via this route where no filesystem
end_comment

begin_comment
comment|// particulars are exposed, redoing our internals will take less effort.
end_comment

begin_comment
comment|//
end_comment

begin_comment
comment|// Currently ClusterSchema Interface will include namespace and table manipulation. Ideally a
end_comment

begin_comment
comment|// form of this Interface will go all the ways down to the file manipulation level but currently
end_comment

begin_comment
comment|// TBD.
end_comment

begin_comment
comment|//
end_comment

begin_comment
comment|// ClusterSchema is private to the Master; only the Master knows current cluster state and has
end_comment

begin_comment
comment|// means of editing/altering it.
end_comment

begin_comment
comment|//
end_comment

begin_comment
comment|// TODO: Remove Server argument when MasterServices are passed.
end_comment

begin_comment
comment|// TODO: We return Future<ProcedureInfo> in the below from most methods. It may change to return
end_comment

begin_comment
comment|// a ProcedureFuture subsequently.
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|ClusterSchema
block|{
comment|/**    * Timeout for cluster operations in milliseconds.    */
specifier|public
specifier|static
specifier|final
name|String
name|HBASE_MASTER_CLUSTER_SCHEMA_OPERATION_TIMEOUT_KEY
init|=
literal|"hbase.master.cluster.schema.operation.timeout"
decl_stmt|;
comment|/**    * Default operation timeout in milliseconds.    */
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_HBASE_MASTER_CLUSTER_SCHEMA_OPERATION_TIMEOUT
init|=
literal|5
operator|*
literal|60
operator|*
literal|1000
decl_stmt|;
comment|/**    * For internals use only. Do not use! Provisionally part of this Interface.    * Prefer the high-level APIs available elsewhere in this API.    * @return Instance of {@link TableNamespaceManager}    */
comment|// TODO: Remove from here. Keep internal. This Interface is too high-level to host this accessor.
name|TableNamespaceManager
name|getTableNamespaceManager
parameter_list|()
function_decl|;
comment|/**    * Create a new Namespace.    * @param namespaceDescriptor descriptor for new Namespace    * @param nonceGroup Identifier for the source of the request, a client or process.    * @param nonce A unique identifier for this operation from the client or process identified by    *<code>nonceGroup</code> (the source must ensure each operation gets a unique id).    * @return procedure id    * @throws IOException Throws {@link ClusterSchemaException} and {@link InterruptedIOException}    *    as well as {@link IOException}    */
name|long
name|createNamespace
parameter_list|(
name|NamespaceDescriptor
name|namespaceDescriptor
parameter_list|,
name|long
name|nonceGroup
parameter_list|,
name|long
name|nonce
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Modify an existing Namespace.    * @param nonceGroup Identifier for the source of the request, a client or process.    * @param nonce A unique identifier for this operation from the client or process identified by    *<code>nonceGroup</code> (the source must ensure each operation gets a unique id).    * @return procedure id    * @throws IOException Throws {@link ClusterSchemaException} and {@link InterruptedIOException}    *    as well as {@link IOException}    */
name|long
name|modifyNamespace
parameter_list|(
name|NamespaceDescriptor
name|descriptor
parameter_list|,
name|long
name|nonceGroup
parameter_list|,
name|long
name|nonce
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Delete an existing Namespace.    * Only empty Namespaces (no tables) can be removed.    * @param nonceGroup Identifier for the source of the request, a client or process.    * @param nonce A unique identifier for this operation from the client or process identified by    *<code>nonceGroup</code> (the source must ensure each operation gets a unique id).    * @return procedure id    * @throws IOException Throws {@link ClusterSchemaException} and {@link InterruptedIOException}    *    as well as {@link IOException}    */
name|long
name|deleteNamespace
parameter_list|(
name|String
name|name
parameter_list|,
name|long
name|nonceGroup
parameter_list|,
name|long
name|nonce
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Get a Namespace    * @param name Name of the Namespace    * @return Namespace descriptor for<code>name</code>    * @throws IOException Throws {@link ClusterSchemaException} and {@link InterruptedIOException}    *    as well as {@link IOException}    */
comment|// No Future here because presumption is that the request will go against cached metadata so
comment|// return immediately -- no need of running a Procedure.
name|NamespaceDescriptor
name|getNamespace
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Get all Namespaces    * @return All Namespace descriptors    */
name|List
argument_list|<
name|NamespaceDescriptor
argument_list|>
name|getNamespaces
parameter_list|()
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

