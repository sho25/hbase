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
name|fs
operator|.
name|FileStatus
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
name|fs
operator|.
name|FileSystem
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
name|HRegionInfo
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
name|catalog
operator|.
name|MetaReader
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
name|HTable
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
name|master
operator|.
name|handler
operator|.
name|CreateTableHandler
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
name|FSUtils
import|;
end_import

begin_comment
comment|/**  * This is a helper class used to manage the namespace  * metadata that is stored in {@see HConstants.NAMESPACE_TABLE_NAME}  * It also mirrors updates to the ZK store by forwarding updates to  * {@link org.apache.hadoop.hbase.ZKNamespaceManager}  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
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
name|HTable
name|table
decl_stmt|;
specifier|private
name|ZKNamespaceManager
name|zkNamespaceManager
decl_stmt|;
specifier|public
name|TableNamespaceManager
parameter_list|(
name|MasterServices
name|masterServices
parameter_list|)
throws|throws
name|IOException
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
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|NAMESPACE_TABLE_NAME
decl_stmt|;
try|try
block|{
if|if
condition|(
operator|!
name|MetaReader
operator|.
name|tableExists
argument_list|(
name|masterServices
operator|.
name|getCatalogTracker
argument_list|()
argument_list|,
name|tableName
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
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Wait for namespace table assignment interrupted"
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|table
operator|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|tableName
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
name|create
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
name|create
argument_list|(
name|NamespaceDescriptor
operator|.
name|SYSTEM_NAMESPACE
argument_list|)
expr_stmt|;
block|}
name|ResultScanner
name|scanner
init|=
name|table
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
name|result
operator|.
name|getColumnLatest
argument_list|(
name|HTableDescriptor
operator|.
name|NAMESPACE_FAMILY_INFO_BYTES
argument_list|,
name|HTableDescriptor
operator|.
name|NAMESPACE_COL_DESC_BYTES
argument_list|)
operator|.
name|getValue
argument_list|()
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
name|res
operator|.
name|getColumnLatest
argument_list|(
name|HTableDescriptor
operator|.
name|NAMESPACE_FAMILY_INFO_BYTES
argument_list|,
name|HTableDescriptor
operator|.
name|NAMESPACE_COL_DESC_BYTES
argument_list|)
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
specifier|public
specifier|synchronized
name|void
name|create
parameter_list|(
name|NamespaceDescriptor
name|ns
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|get
argument_list|(
name|ns
operator|.
name|getName
argument_list|()
argument_list|)
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|ConstraintException
argument_list|(
literal|"Namespace "
operator|+
name|ns
operator|.
name|getName
argument_list|()
operator|+
literal|" already exists"
argument_list|)
throw|;
block|}
name|FileSystem
name|fs
init|=
name|masterServices
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
name|fs
operator|.
name|mkdirs
argument_list|(
name|FSUtils
operator|.
name|getNamespaceDir
argument_list|(
name|masterServices
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|getRootDir
argument_list|()
argument_list|,
name|ns
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|upsert
argument_list|(
name|ns
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|void
name|update
parameter_list|(
name|NamespaceDescriptor
name|ns
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|get
argument_list|(
name|ns
operator|.
name|getName
argument_list|()
argument_list|)
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ConstraintException
argument_list|(
literal|"Namespace "
operator|+
name|ns
operator|.
name|getName
argument_list|()
operator|+
literal|" does not exist"
argument_list|)
throw|;
block|}
name|upsert
argument_list|(
name|ns
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|upsert
parameter_list|(
name|NamespaceDescriptor
name|ns
parameter_list|)
throws|throws
name|IOException
block|{
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
name|add
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
name|table
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
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
literal|"Failed to update namespace information in ZK. Aborting."
decl_stmt|;
name|LOG
operator|.
name|fatal
argument_list|(
name|msg
argument_list|,
name|ex
argument_list|)
expr_stmt|;
name|masterServices
operator|.
name|abort
argument_list|(
name|msg
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|synchronized
name|void
name|remove
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|NamespaceDescriptor
operator|.
name|RESERVED_NAMESPACES
operator|.
name|contains
argument_list|(
name|name
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|ConstraintException
argument_list|(
literal|"Reserved namespace "
operator|+
name|name
operator|+
literal|" cannot be removed."
argument_list|)
throw|;
block|}
name|int
name|tableCount
init|=
name|masterServices
operator|.
name|getTableDescriptorsByNamespace
argument_list|(
name|name
argument_list|)
operator|.
name|size
argument_list|()
decl_stmt|;
if|if
condition|(
name|tableCount
operator|>
literal|0
condition|)
block|{
throw|throw
operator|new
name|ConstraintException
argument_list|(
literal|"Only empty namespaces can be removed. "
operator|+
literal|"Namespace "
operator|+
name|name
operator|+
literal|" has "
operator|+
name|tableCount
operator|+
literal|" tables"
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
name|name
argument_list|)
argument_list|)
decl_stmt|;
name|table
operator|.
name|delete
argument_list|(
name|d
argument_list|)
expr_stmt|;
comment|//don't abort if cleanup isn't complete
comment|//it will be replaced on new namespace creation
name|zkNamespaceManager
operator|.
name|remove
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|FileSystem
name|fs
init|=
name|masterServices
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
for|for
control|(
name|FileStatus
name|status
range|:
name|fs
operator|.
name|listStatus
argument_list|(
name|FSUtils
operator|.
name|getNamespaceDir
argument_list|(
name|masterServices
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|getRootDir
argument_list|()
argument_list|,
name|name
argument_list|)
argument_list|)
control|)
block|{
if|if
condition|(
operator|!
name|HConstants
operator|.
name|HBASE_NON_TABLE_DIRS
operator|.
name|contains
argument_list|(
name|status
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Namespace directory contains table dir: "
operator|+
name|status
operator|.
name|getPath
argument_list|()
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
operator|!
name|fs
operator|.
name|delete
argument_list|(
name|FSUtils
operator|.
name|getNamespaceDir
argument_list|(
name|masterServices
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|getRootDir
argument_list|()
argument_list|,
name|name
argument_list|)
argument_list|,
literal|true
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to remove namespace: "
operator|+
name|name
argument_list|)
throw|;
block|}
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
name|table
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
name|r
operator|.
name|getColumnLatest
argument_list|(
name|HTableDescriptor
operator|.
name|NAMESPACE_FAMILY_INFO_BYTES
argument_list|,
name|HTableDescriptor
operator|.
name|NAMESPACE_COL_DESC_BYTES
argument_list|)
operator|.
name|getValue
argument_list|()
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
throws|,
name|InterruptedException
block|{
name|HRegionInfo
name|newRegions
index|[]
init|=
operator|new
name|HRegionInfo
index|[]
block|{
operator|new
name|HRegionInfo
argument_list|(
name|HTableDescriptor
operator|.
name|NAMESPACE_TABLEDESC
operator|.
name|getTableName
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
block|}
decl_stmt|;
comment|//we need to create the table this way to bypass
comment|//checkInitialized
name|masterServices
operator|.
name|getExecutorService
argument_list|()
operator|.
name|submit
argument_list|(
operator|new
name|CreateTableHandler
argument_list|(
name|masterServices
argument_list|,
name|masterServices
operator|.
name|getMasterFileSystem
argument_list|()
argument_list|,
name|HTableDescriptor
operator|.
name|NAMESPACE_TABLEDESC
argument_list|,
name|masterServices
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|newRegions
argument_list|,
name|masterServices
argument_list|)
operator|.
name|prepare
argument_list|()
argument_list|)
expr_stmt|;
comment|//wait for region to be online
name|int
name|tries
init|=
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.master.namespace.init.timeout"
argument_list|,
literal|600
argument_list|)
decl_stmt|;
while|while
condition|(
name|masterServices
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionStates
argument_list|()
operator|.
name|getRegionServerOfRegion
argument_list|(
name|newRegions
index|[
literal|0
index|]
argument_list|)
operator|==
literal|null
operator|&&
name|tries
operator|>
literal|0
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
name|tries
operator|--
expr_stmt|;
block|}
if|if
condition|(
name|tries
operator|<=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to create namespace table."
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

