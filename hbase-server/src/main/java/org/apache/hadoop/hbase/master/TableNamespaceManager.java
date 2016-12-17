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
name|NavigableSet
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang
operator|.
name|StringUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|conf
operator|.
name|Configuration
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
name|CellUtil
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
name|DoNotRetryIOException
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
name|HConstants
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
name|HTableDescriptor
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
name|MetaTableAccessor
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
name|ZKNamespaceManager
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
name|client
operator|.
name|Delete
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
name|Get
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
name|Put
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
name|Result
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
name|ResultScanner
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
name|Table
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
name|TableState
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
name|constraint
operator|.
name|ConstraintException
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
name|exceptions
operator|.
name|TimeoutIOException
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
name|master
operator|.
name|procedure
operator|.
name|MasterProcedureEnv
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
name|procedure2
operator|.
name|ProcedureExecutor
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
name|ProtobufUtil
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
name|HBaseProtos
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
name|util
operator|.
name|Bytes
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
name|util
operator|.
name|EnvironmentEdgeManager
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
name|util
operator|.
name|Threads
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Sets
import|;
end_import

begin_comment
comment|/**  * This is a helper class used internally to manage the namespace metadata that is stored in  * TableName.NAMESPACE_TABLE_NAME. It also mirrors updates to the ZK store by forwarding updates to  * {@link org.apache.hadoop.hbase.ZKNamespaceManager}.  *  * WARNING: Do not use. Go via the higher-level {@link ClusterSchema} API instead. This manager  * is likely to go aways anyways.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|edu
operator|.
name|umd
operator|.
name|cs
operator|.
name|findbugs
operator|.
name|annotations
operator|.
name|SuppressWarnings
argument_list|(
name|value
operator|=
literal|"IS2_INCONSISTENT_SYNC"
argument_list|,
name|justification
operator|=
literal|"TODO: synchronize access on nsTable but it is done in tiers above and this "
operator|+
literal|"class is going away/shrinking"
argument_list|)
specifier|public
class|class
name|TableNamespaceManager
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TableNamespaceManager
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|MasterServices
name|masterServices
decl_stmt|;
specifier|private
name|Table
name|nsTable
init|=
literal|null
decl_stmt|;
comment|// FindBugs: IS2_INCONSISTENT_SYNC TODO: Access is not synchronized
specifier|private
name|ZKNamespaceManager
name|zkNamespaceManager
decl_stmt|;
specifier|private
name|boolean
name|initialized
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|KEY_MAX_REGIONS
init|=
literal|"hbase.namespace.quota.maxregions"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|KEY_MAX_TABLES
init|=
literal|"hbase.namespace.quota.maxtables"
decl_stmt|;
specifier|static
specifier|final
name|String
name|NS_INIT_TIMEOUT
init|=
literal|"hbase.master.namespace.init.timeout"
decl_stmt|;
specifier|static
specifier|final
name|int
name|DEFAULT_NS_INIT_TIMEOUT
init|=
literal|300000
decl_stmt|;
name|TableNamespaceManager
parameter_list|(
name|MasterServices
name|masterServices
parameter_list|)
block|{
name|this
operator|.
name|masterServices
operator|=
name|masterServices
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|masterServices
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|start
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|MetaTableAccessor
operator|.
name|tableExists
argument_list|(
name|masterServices
operator|.
name|getConnection
argument_list|()
argument_list|,
name|TableName
operator|.
name|NAMESPACE_TABLE_NAME
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Namespace table not found. Creating..."
argument_list|)
expr_stmt|;
name|createNamespaceTable
argument_list|(
name|masterServices
argument_list|)
expr_stmt|;
block|}
try|try
block|{
comment|// Wait for the namespace table to be initialized.
name|long
name|startTime
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
name|int
name|timeout
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|NS_INIT_TIMEOUT
argument_list|,
name|DEFAULT_NS_INIT_TIMEOUT
argument_list|)
decl_stmt|;
while|while
condition|(
operator|!
name|isTableAvailableAndInitialized
argument_list|()
condition|)
block|{
if|if
condition|(
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
operator|-
name|startTime
operator|+
literal|100
operator|>
name|timeout
condition|)
block|{
comment|// We can't do anything if ns is not online.
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Timedout "
operator|+
name|timeout
operator|+
literal|"ms waiting for namespace table to "
operator|+
literal|"be assigned and enabled: "
operator|+
name|getTableState
argument_list|()
argument_list|)
throw|;
block|}
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|(
name|InterruptedIOException
operator|)
operator|new
name|InterruptedIOException
argument_list|()
operator|.
name|initCause
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
specifier|synchronized
name|Table
name|getNamespaceTable
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|isTableNamespaceManagerInitialized
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|" isn't ready to serve"
argument_list|)
throw|;
block|}
return|return
name|nsTable
return|;
block|}
comment|/*    * check whether a namespace has already existed.    */
specifier|public
name|boolean
name|doesNamespaceExist
parameter_list|(
specifier|final
name|String
name|namespaceName
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|nsTable
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|" isn't ready to serve"
argument_list|)
throw|;
block|}
return|return
operator|(
name|get
argument_list|(
name|nsTable
argument_list|,
name|namespaceName
argument_list|)
operator|!=
literal|null
operator|)
return|;
block|}
specifier|public
specifier|synchronized
name|NamespaceDescriptor
name|get
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|isTableNamespaceManagerInitialized
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|zkNamespaceManager
operator|.
name|get
argument_list|(
name|name
argument_list|)
return|;
block|}
specifier|private
name|NamespaceDescriptor
name|get
parameter_list|(
name|Table
name|table
parameter_list|,
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
name|Result
name|res
init|=
name|table
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|name
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|res
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
name|byte
index|[]
name|val
init|=
name|CellUtil
operator|.
name|cloneValue
argument_list|(
name|res
operator|.
name|getColumnLatestCell
argument_list|(
name|HTableDescriptor
operator|.
name|NAMESPACE_FAMILY_INFO_BYTES
argument_list|,
name|HTableDescriptor
operator|.
name|NAMESPACE_COL_DESC_BYTES
argument_list|)
argument_list|)
decl_stmt|;
return|return
name|ProtobufUtil
operator|.
name|toNamespaceDescriptor
argument_list|(
name|HBaseProtos
operator|.
name|NamespaceDescriptor
operator|.
name|parseFrom
argument_list|(
name|val
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|void
name|insertIntoNSTable
parameter_list|(
specifier|final
name|NamespaceDescriptor
name|ns
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|nsTable
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|" isn't ready to serve"
argument_list|)
throw|;
block|}
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|ns
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|p
operator|.
name|addImmutable
argument_list|(
name|HTableDescriptor
operator|.
name|NAMESPACE_FAMILY_INFO_BYTES
argument_list|,
name|HTableDescriptor
operator|.
name|NAMESPACE_COL_DESC_BYTES
argument_list|,
name|ProtobufUtil
operator|.
name|toProtoNamespaceDescriptor
argument_list|(
name|ns
argument_list|)
operator|.
name|toByteArray
argument_list|()
argument_list|)
expr_stmt|;
name|nsTable
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|updateZKNamespaceManager
parameter_list|(
specifier|final
name|NamespaceDescriptor
name|ns
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|zkNamespaceManager
operator|.
name|update
argument_list|(
name|ns
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
name|String
name|msg
init|=
literal|"Failed to update namespace information in ZK."
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|msg
argument_list|,
name|ex
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
name|msg
argument_list|,
name|ex
argument_list|)
throw|;
block|}
block|}
specifier|public
name|void
name|removeFromNSTable
parameter_list|(
specifier|final
name|String
name|namespaceName
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|nsTable
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|" isn't ready to serve"
argument_list|)
throw|;
block|}
name|Delete
name|d
init|=
operator|new
name|Delete
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|namespaceName
argument_list|)
argument_list|)
decl_stmt|;
name|nsTable
operator|.
name|delete
argument_list|(
name|d
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|removeFromZKNamespaceManager
parameter_list|(
specifier|final
name|String
name|namespaceName
parameter_list|)
throws|throws
name|IOException
block|{
name|zkNamespaceManager
operator|.
name|remove
argument_list|(
name|namespaceName
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|NavigableSet
argument_list|<
name|NamespaceDescriptor
argument_list|>
name|list
parameter_list|()
throws|throws
name|IOException
block|{
name|NavigableSet
argument_list|<
name|NamespaceDescriptor
argument_list|>
name|ret
init|=
name|Sets
operator|.
name|newTreeSet
argument_list|(
name|NamespaceDescriptor
operator|.
name|NAMESPACE_DESCRIPTOR_COMPARATOR
argument_list|)
decl_stmt|;
name|ResultScanner
name|scanner
init|=
name|getNamespaceTable
argument_list|()
operator|.
name|getScanner
argument_list|(
name|HTableDescriptor
operator|.
name|NAMESPACE_FAMILY_INFO_BYTES
argument_list|)
decl_stmt|;
try|try
block|{
for|for
control|(
name|Result
name|r
range|:
name|scanner
control|)
block|{
name|byte
index|[]
name|val
init|=
name|CellUtil
operator|.
name|cloneValue
argument_list|(
name|r
operator|.
name|getColumnLatestCell
argument_list|(
name|HTableDescriptor
operator|.
name|NAMESPACE_FAMILY_INFO_BYTES
argument_list|,
name|HTableDescriptor
operator|.
name|NAMESPACE_COL_DESC_BYTES
argument_list|)
argument_list|)
decl_stmt|;
name|ret
operator|.
name|add
argument_list|(
name|ProtobufUtil
operator|.
name|toNamespaceDescriptor
argument_list|(
name|HBaseProtos
operator|.
name|NamespaceDescriptor
operator|.
name|parseFrom
argument_list|(
name|val
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
specifier|private
name|void
name|createNamespaceTable
parameter_list|(
name|MasterServices
name|masterServices
parameter_list|)
throws|throws
name|IOException
block|{
name|masterServices
operator|.
name|createSystemTable
argument_list|(
name|HTableDescriptor
operator|.
name|NAMESPACE_TABLEDESC
argument_list|)
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
specifier|private
name|boolean
name|isTableNamespaceManagerInitialized
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|initialized
condition|)
block|{
name|this
operator|.
name|nsTable
operator|=
name|this
operator|.
name|masterServices
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|TableName
operator|.
name|NAMESPACE_TABLE_NAME
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
comment|/**    * Create Namespace in a blocking manner. Keeps trying until    * {@link ClusterSchema.HBASE_MASTER_CLUSTER_SCHEMA_OPERATION_TIMEOUT_KEY} expires.    * Note, by-passes notifying coprocessors and name checks. Use for system namespaces only.    */
specifier|private
name|void
name|blockingCreateNamespace
parameter_list|(
specifier|final
name|NamespaceDescriptor
name|namespaceDescriptor
parameter_list|)
throws|throws
name|IOException
block|{
name|ClusterSchema
name|clusterSchema
init|=
name|this
operator|.
name|masterServices
operator|.
name|getClusterSchema
argument_list|()
decl_stmt|;
name|long
name|procId
init|=
name|clusterSchema
operator|.
name|createNamespace
argument_list|(
name|namespaceDescriptor
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|block
argument_list|(
name|this
operator|.
name|masterServices
argument_list|,
name|procId
argument_list|)
expr_stmt|;
block|}
comment|/**    * An ugly utility to be removed when refactor TableNamespaceManager.    * @throws TimeoutIOException    */
specifier|private
specifier|static
name|void
name|block
parameter_list|(
specifier|final
name|MasterServices
name|services
parameter_list|,
specifier|final
name|long
name|procId
parameter_list|)
throws|throws
name|TimeoutIOException
block|{
name|int
name|timeoutInMillis
init|=
name|services
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getInt
argument_list|(
name|ClusterSchema
operator|.
name|HBASE_MASTER_CLUSTER_SCHEMA_OPERATION_TIMEOUT_KEY
argument_list|,
name|ClusterSchema
operator|.
name|DEFAULT_HBASE_MASTER_CLUSTER_SCHEMA_OPERATION_TIMEOUT
argument_list|)
decl_stmt|;
name|long
name|deadlineTs
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
operator|+
name|timeoutInMillis
decl_stmt|;
name|ProcedureExecutor
argument_list|<
name|MasterProcedureEnv
argument_list|>
name|procedureExecutor
init|=
name|services
operator|.
name|getMasterProcedureExecutor
argument_list|()
decl_stmt|;
while|while
condition|(
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
operator|<
name|deadlineTs
condition|)
block|{
if|if
condition|(
name|procedureExecutor
operator|.
name|isFinished
argument_list|(
name|procId
argument_list|)
condition|)
return|return;
comment|// Sleep some
name|Threads
operator|.
name|sleep
argument_list|(
literal|10
argument_list|)
expr_stmt|;
block|}
throw|throw
operator|new
name|TimeoutIOException
argument_list|(
literal|"Procedure "
operator|+
name|procId
operator|+
literal|" is still running"
argument_list|)
throw|;
block|}
comment|/**    * This method checks if the namespace table is assigned and then    * tries to create its Table reference. If it was already created before, it also makes    * sure that the connection isn't closed.    * @return true if the namespace table manager is ready to serve, false otherwise    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
specifier|public
specifier|synchronized
name|boolean
name|isTableAvailableAndInitialized
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Did we already get a table? If so, still make sure it's available
if|if
condition|(
name|isTableNamespaceManagerInitialized
argument_list|()
condition|)
block|{
return|return
literal|true
return|;
block|}
comment|// Now check if the table is assigned, if not then fail fast
if|if
condition|(
name|isTableAssigned
argument_list|()
operator|&&
name|isTableEnabled
argument_list|()
condition|)
block|{
try|try
block|{
name|boolean
name|initGoodSofar
init|=
literal|true
decl_stmt|;
name|nsTable
operator|=
name|this
operator|.
name|masterServices
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|TableName
operator|.
name|NAMESPACE_TABLE_NAME
argument_list|)
expr_stmt|;
name|zkNamespaceManager
operator|=
operator|new
name|ZKNamespaceManager
argument_list|(
name|masterServices
operator|.
name|getZooKeeper
argument_list|()
argument_list|)
expr_stmt|;
name|zkNamespaceManager
operator|.
name|start
argument_list|()
expr_stmt|;
if|if
condition|(
name|get
argument_list|(
name|nsTable
argument_list|,
name|NamespaceDescriptor
operator|.
name|DEFAULT_NAMESPACE
operator|.
name|getName
argument_list|()
argument_list|)
operator|==
literal|null
condition|)
block|{
name|blockingCreateNamespace
argument_list|(
name|NamespaceDescriptor
operator|.
name|DEFAULT_NAMESPACE
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|get
argument_list|(
name|nsTable
argument_list|,
name|NamespaceDescriptor
operator|.
name|SYSTEM_NAMESPACE
operator|.
name|getName
argument_list|()
argument_list|)
operator|==
literal|null
condition|)
block|{
name|blockingCreateNamespace
argument_list|(
name|NamespaceDescriptor
operator|.
name|SYSTEM_NAMESPACE
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|initGoodSofar
condition|)
block|{
comment|// some required namespace is created asynchronized. We should complete init later.
return|return
literal|false
return|;
block|}
name|ResultScanner
name|scanner
init|=
name|nsTable
operator|.
name|getScanner
argument_list|(
name|HTableDescriptor
operator|.
name|NAMESPACE_FAMILY_INFO_BYTES
argument_list|)
decl_stmt|;
try|try
block|{
for|for
control|(
name|Result
name|result
range|:
name|scanner
control|)
block|{
name|byte
index|[]
name|val
init|=
name|CellUtil
operator|.
name|cloneValue
argument_list|(
name|result
operator|.
name|getColumnLatestCell
argument_list|(
name|HTableDescriptor
operator|.
name|NAMESPACE_FAMILY_INFO_BYTES
argument_list|,
name|HTableDescriptor
operator|.
name|NAMESPACE_COL_DESC_BYTES
argument_list|)
argument_list|)
decl_stmt|;
name|NamespaceDescriptor
name|ns
init|=
name|ProtobufUtil
operator|.
name|toNamespaceDescriptor
argument_list|(
name|HBaseProtos
operator|.
name|NamespaceDescriptor
operator|.
name|parseFrom
argument_list|(
name|val
argument_list|)
argument_list|)
decl_stmt|;
name|zkNamespaceManager
operator|.
name|update
argument_list|(
name|ns
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|initialized
operator|=
literal|true
expr_stmt|;
return|return
literal|true
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ie
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Caught exception in initializing namespace table manager"
argument_list|,
name|ie
argument_list|)
expr_stmt|;
if|if
condition|(
name|nsTable
operator|!=
literal|null
condition|)
block|{
name|nsTable
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
throw|throw
name|ie
throw|;
block|}
block|}
return|return
literal|false
return|;
block|}
specifier|private
name|TableState
operator|.
name|State
name|getTableState
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|masterServices
operator|.
name|getTableStateManager
argument_list|()
operator|.
name|getTableState
argument_list|(
name|TableName
operator|.
name|NAMESPACE_TABLE_NAME
argument_list|)
return|;
block|}
specifier|private
name|boolean
name|isTableEnabled
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|getTableState
argument_list|()
operator|.
name|equals
argument_list|(
name|TableState
operator|.
name|State
operator|.
name|ENABLED
argument_list|)
return|;
block|}
specifier|private
name|boolean
name|isTableAssigned
parameter_list|()
block|{
return|return
operator|!
name|masterServices
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionStates
argument_list|()
operator|.
name|getRegionsOfTable
argument_list|(
name|TableName
operator|.
name|NAMESPACE_TABLE_NAME
argument_list|)
operator|.
name|isEmpty
argument_list|()
return|;
block|}
specifier|public
name|void
name|validateTableAndRegionCount
parameter_list|(
name|NamespaceDescriptor
name|desc
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|getMaxRegions
argument_list|(
name|desc
argument_list|)
operator|<=
literal|0
condition|)
block|{
throw|throw
operator|new
name|ConstraintException
argument_list|(
literal|"The max region quota for "
operator|+
name|desc
operator|.
name|getName
argument_list|()
operator|+
literal|" is less than or equal to zero."
argument_list|)
throw|;
block|}
if|if
condition|(
name|getMaxTables
argument_list|(
name|desc
argument_list|)
operator|<=
literal|0
condition|)
block|{
throw|throw
operator|new
name|ConstraintException
argument_list|(
literal|"The max tables quota for "
operator|+
name|desc
operator|.
name|getName
argument_list|()
operator|+
literal|" is less than or equal to zero."
argument_list|)
throw|;
block|}
block|}
specifier|public
specifier|static
name|long
name|getMaxTables
parameter_list|(
name|NamespaceDescriptor
name|ns
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|value
init|=
name|ns
operator|.
name|getConfigurationValue
argument_list|(
name|KEY_MAX_TABLES
argument_list|)
decl_stmt|;
name|long
name|maxTables
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|StringUtils
operator|.
name|isNotEmpty
argument_list|(
name|value
argument_list|)
condition|)
block|{
try|try
block|{
name|maxTables
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|exp
parameter_list|)
block|{
throw|throw
operator|new
name|DoNotRetryIOException
argument_list|(
literal|"NumberFormatException while getting max tables."
argument_list|,
name|exp
argument_list|)
throw|;
block|}
block|}
else|else
block|{
comment|// The property is not set, so assume its the max long value.
name|maxTables
operator|=
name|Long
operator|.
name|MAX_VALUE
expr_stmt|;
block|}
return|return
name|maxTables
return|;
block|}
specifier|public
specifier|static
name|long
name|getMaxRegions
parameter_list|(
name|NamespaceDescriptor
name|ns
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|value
init|=
name|ns
operator|.
name|getConfigurationValue
argument_list|(
name|KEY_MAX_REGIONS
argument_list|)
decl_stmt|;
name|long
name|maxRegions
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|StringUtils
operator|.
name|isNotEmpty
argument_list|(
name|value
argument_list|)
condition|)
block|{
try|try
block|{
name|maxRegions
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|exp
parameter_list|)
block|{
throw|throw
operator|new
name|DoNotRetryIOException
argument_list|(
literal|"NumberFormatException while getting max regions."
argument_list|,
name|exp
argument_list|)
throw|;
block|}
block|}
else|else
block|{
comment|// The property is not set, so assume its the max long value.
name|maxRegions
operator|=
name|Long
operator|.
name|MAX_VALUE
expr_stmt|;
block|}
return|return
name|maxRegions
return|;
block|}
block|}
end_class

end_unit

