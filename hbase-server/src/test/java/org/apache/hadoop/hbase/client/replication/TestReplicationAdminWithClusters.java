begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one or more contributor license  * agreements. See the NOTICE file distributed with this work for additional information regarding  * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License. You may obtain a  * copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable  * law or agreed to in writing, software distributed under the License is distributed on an "AS IS"  * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License  * for the specific language governing permissions and limitations under the License.  */
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
name|client
operator|.
name|replication
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertFalse
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|fail
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|UUID
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
name|HColumnDescriptor
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
name|TableNotFoundException
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
name|Admin
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
name|Connection
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
name|ConnectionFactory
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
name|BaseReplicationEndpoint
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
name|ReplicationPeerConfig
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
name|TestReplicationBase
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
name|testclassification
operator|.
name|ClientTests
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
name|testclassification
operator|.
name|MediumTests
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Rule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|rules
operator|.
name|TestName
import|;
end_import

begin_comment
comment|/**  * Unit testing of ReplicationAdmin with clusters  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MediumTests
operator|.
name|class
block|,
name|ClientTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestReplicationAdminWithClusters
extends|extends
name|TestReplicationBase
block|{
specifier|static
name|Connection
name|connection1
decl_stmt|;
specifier|static
name|Connection
name|connection2
decl_stmt|;
specifier|static
name|Admin
name|admin1
decl_stmt|;
specifier|static
name|Admin
name|admin2
decl_stmt|;
specifier|static
name|ReplicationAdmin
name|adminExt
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|TestName
name|name
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|TestReplicationBase
operator|.
name|setUpBeforeClass
argument_list|()
expr_stmt|;
name|connection1
operator|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf1
argument_list|)
expr_stmt|;
name|connection2
operator|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf2
argument_list|)
expr_stmt|;
name|admin1
operator|=
name|connection1
operator|.
name|getAdmin
argument_list|()
expr_stmt|;
name|admin2
operator|=
name|connection2
operator|.
name|getAdmin
argument_list|()
expr_stmt|;
name|adminExt
operator|=
operator|new
name|ReplicationAdmin
argument_list|(
name|conf1
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDownAfterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|admin1
operator|.
name|close
argument_list|()
expr_stmt|;
name|admin2
operator|.
name|close
argument_list|()
expr_stmt|;
name|adminExt
operator|.
name|close
argument_list|()
expr_stmt|;
name|connection1
operator|.
name|close
argument_list|()
expr_stmt|;
name|connection2
operator|.
name|close
argument_list|()
expr_stmt|;
name|TestReplicationBase
operator|.
name|tearDownAfterClass
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|300000
argument_list|)
specifier|public
name|void
name|testEnableReplicationWhenSlaveClusterDoesntHaveTable
parameter_list|()
throws|throws
name|Exception
block|{
name|admin1
operator|.
name|disableTableReplication
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|admin2
operator|.
name|disableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|admin2
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|admin2
operator|.
name|tableExists
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
name|admin1
operator|.
name|enableTableReplication
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|admin2
operator|.
name|tableExists
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|300000
argument_list|)
specifier|public
name|void
name|testEnableReplicationWhenReplicationNotEnabled
parameter_list|()
throws|throws
name|Exception
block|{
name|HTableDescriptor
name|table
init|=
name|admin1
operator|.
name|getTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
for|for
control|(
name|HColumnDescriptor
name|fam
range|:
name|table
operator|.
name|getColumnFamilies
argument_list|()
control|)
block|{
name|fam
operator|.
name|setScope
argument_list|(
name|HConstants
operator|.
name|REPLICATION_SCOPE_LOCAL
argument_list|)
expr_stmt|;
block|}
name|admin1
operator|.
name|disableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|admin1
operator|.
name|modifyTable
argument_list|(
name|tableName
argument_list|,
name|table
argument_list|)
expr_stmt|;
name|admin1
operator|.
name|enableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|admin2
operator|.
name|disableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|admin2
operator|.
name|modifyTable
argument_list|(
name|tableName
argument_list|,
name|table
argument_list|)
expr_stmt|;
name|admin2
operator|.
name|enableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|admin1
operator|.
name|enableTableReplication
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|table
operator|=
name|admin1
operator|.
name|getTableDescriptor
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
for|for
control|(
name|HColumnDescriptor
name|fam
range|:
name|table
operator|.
name|getColumnFamilies
argument_list|()
control|)
block|{
name|assertEquals
argument_list|(
name|fam
operator|.
name|getScope
argument_list|()
argument_list|,
name|HConstants
operator|.
name|REPLICATION_SCOPE_GLOBAL
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|300000
argument_list|)
specifier|public
name|void
name|testEnableReplicationWhenTableDescriptorIsNotSameInClusters
parameter_list|()
throws|throws
name|Exception
block|{
name|HTableDescriptor
name|table
init|=
name|admin2
operator|.
name|getTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|HColumnDescriptor
name|f
init|=
operator|new
name|HColumnDescriptor
argument_list|(
literal|"newFamily"
argument_list|)
decl_stmt|;
name|table
operator|.
name|addFamily
argument_list|(
name|f
argument_list|)
expr_stmt|;
name|admin2
operator|.
name|disableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|admin2
operator|.
name|modifyTable
argument_list|(
name|tableName
argument_list|,
name|table
argument_list|)
expr_stmt|;
name|admin2
operator|.
name|enableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
try|try
block|{
name|admin1
operator|.
name|enableTableReplication
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Exception should be thrown if table descriptors in the clusters are not same."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RuntimeException
name|ignored
parameter_list|)
block|{      }
name|admin1
operator|.
name|disableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|admin1
operator|.
name|modifyTable
argument_list|(
name|tableName
argument_list|,
name|table
argument_list|)
expr_stmt|;
name|admin1
operator|.
name|enableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|admin1
operator|.
name|enableTableReplication
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|table
operator|=
name|admin1
operator|.
name|getTableDescriptor
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
for|for
control|(
name|HColumnDescriptor
name|fam
range|:
name|table
operator|.
name|getColumnFamilies
argument_list|()
control|)
block|{
name|assertEquals
argument_list|(
name|fam
operator|.
name|getScope
argument_list|()
argument_list|,
name|HConstants
operator|.
name|REPLICATION_SCOPE_GLOBAL
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|300000
argument_list|)
specifier|public
name|void
name|testDisableAndEnableReplication
parameter_list|()
throws|throws
name|Exception
block|{
name|admin1
operator|.
name|disableTableReplication
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|HTableDescriptor
name|table
init|=
name|admin1
operator|.
name|getTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
for|for
control|(
name|HColumnDescriptor
name|fam
range|:
name|table
operator|.
name|getColumnFamilies
argument_list|()
control|)
block|{
name|assertEquals
argument_list|(
name|fam
operator|.
name|getScope
argument_list|()
argument_list|,
name|HConstants
operator|.
name|REPLICATION_SCOPE_LOCAL
argument_list|)
expr_stmt|;
block|}
name|table
operator|=
name|admin2
operator|.
name|getTableDescriptor
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
for|for
control|(
name|HColumnDescriptor
name|fam
range|:
name|table
operator|.
name|getColumnFamilies
argument_list|()
control|)
block|{
name|assertEquals
argument_list|(
name|fam
operator|.
name|getScope
argument_list|()
argument_list|,
name|HConstants
operator|.
name|REPLICATION_SCOPE_LOCAL
argument_list|)
expr_stmt|;
block|}
name|admin1
operator|.
name|enableTableReplication
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|table
operator|=
name|admin1
operator|.
name|getTableDescriptor
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
for|for
control|(
name|HColumnDescriptor
name|fam
range|:
name|table
operator|.
name|getColumnFamilies
argument_list|()
control|)
block|{
name|assertEquals
argument_list|(
name|fam
operator|.
name|getScope
argument_list|()
argument_list|,
name|HConstants
operator|.
name|REPLICATION_SCOPE_GLOBAL
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|300000
argument_list|,
name|expected
operator|=
name|TableNotFoundException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testDisableReplicationForNonExistingTable
parameter_list|()
throws|throws
name|Exception
block|{
name|admin1
operator|.
name|disableTableReplication
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|300000
argument_list|,
name|expected
operator|=
name|TableNotFoundException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testEnableReplicationForNonExistingTable
parameter_list|()
throws|throws
name|Exception
block|{
name|admin1
operator|.
name|enableTableReplication
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|300000
argument_list|,
name|expected
operator|=
name|IllegalArgumentException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testDisableReplicationWhenTableNameAsNull
parameter_list|()
throws|throws
name|Exception
block|{
name|admin1
operator|.
name|disableTableReplication
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|300000
argument_list|,
name|expected
operator|=
name|IllegalArgumentException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testEnableReplicationWhenTableNameAsNull
parameter_list|()
throws|throws
name|Exception
block|{
name|admin1
operator|.
name|enableTableReplication
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/*    * Test enable table replication should create table only in user explicit specified table-cfs.    * HBASE-14717    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|300000
argument_list|)
specifier|public
name|void
name|testEnableReplicationForExplicitSetTableCfs
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|peerId
init|=
literal|"2"
decl_stmt|;
if|if
condition|(
name|admin2
operator|.
name|isTableAvailable
argument_list|(
name|TestReplicationBase
operator|.
name|tableName
argument_list|)
condition|)
block|{
name|admin2
operator|.
name|disableTable
argument_list|(
name|TestReplicationBase
operator|.
name|tableName
argument_list|)
expr_stmt|;
name|admin2
operator|.
name|deleteTable
argument_list|(
name|TestReplicationBase
operator|.
name|tableName
argument_list|)
expr_stmt|;
block|}
name|assertFalse
argument_list|(
literal|"Table should not exists in the peer cluster"
argument_list|,
name|admin2
operator|.
name|isTableAvailable
argument_list|(
name|TestReplicationBase
operator|.
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|TableName
argument_list|,
name|?
extends|extends
name|Collection
argument_list|<
name|String
argument_list|>
argument_list|>
name|tableCfs
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|tableCfs
operator|.
name|put
argument_list|(
name|tableName
argument_list|,
literal|null
argument_list|)
expr_stmt|;
try|try
block|{
name|adminExt
operator|.
name|setPeerTableCFs
argument_list|(
name|peerId
argument_list|,
name|tableCfs
argument_list|)
expr_stmt|;
name|admin1
operator|.
name|enableTableReplication
argument_list|(
name|TestReplicationBase
operator|.
name|tableName
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"Table should not be created if user has set table cfs explicitly for the "
operator|+
literal|"peer and this is not part of that collection"
argument_list|,
name|admin2
operator|.
name|isTableAvailable
argument_list|(
name|TestReplicationBase
operator|.
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
name|tableCfs
operator|.
name|put
argument_list|(
name|TestReplicationBase
operator|.
name|tableName
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|adminExt
operator|.
name|setPeerTableCFs
argument_list|(
name|peerId
argument_list|,
name|tableCfs
argument_list|)
expr_stmt|;
name|admin1
operator|.
name|enableTableReplication
argument_list|(
name|TestReplicationBase
operator|.
name|tableName
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Table should be created if user has explicitly added table into table cfs collection"
argument_list|,
name|admin2
operator|.
name|isTableAvailable
argument_list|(
name|TestReplicationBase
operator|.
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|adminExt
operator|.
name|removePeerTableCFs
argument_list|(
name|peerId
argument_list|,
name|adminExt
operator|.
name|getPeerTableCFs
argument_list|(
name|peerId
argument_list|)
argument_list|)
expr_stmt|;
name|admin1
operator|.
name|disableTableReplication
argument_list|(
name|TestReplicationBase
operator|.
name|tableName
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|300000
argument_list|)
specifier|public
name|void
name|testReplicationPeerConfigUpdateCallback
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|peerId
init|=
literal|"1"
decl_stmt|;
name|ReplicationPeerConfig
name|rpc
init|=
operator|new
name|ReplicationPeerConfig
argument_list|()
decl_stmt|;
name|rpc
operator|.
name|setClusterKey
argument_list|(
name|utility2
operator|.
name|getClusterKey
argument_list|()
argument_list|)
expr_stmt|;
name|rpc
operator|.
name|setReplicationEndpointImpl
argument_list|(
name|TestUpdatableReplicationEndpoint
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|rpc
operator|.
name|getConfiguration
argument_list|()
operator|.
name|put
argument_list|(
literal|"key1"
argument_list|,
literal|"value1"
argument_list|)
expr_stmt|;
name|admin
operator|.
name|addPeer
argument_list|(
name|peerId
argument_list|,
name|rpc
argument_list|)
expr_stmt|;
name|admin
operator|.
name|peerAdded
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
name|rpc
operator|.
name|getConfiguration
argument_list|()
operator|.
name|put
argument_list|(
literal|"key1"
argument_list|,
literal|"value2"
argument_list|)
expr_stmt|;
name|admin
operator|.
name|updatePeerConfig
argument_list|(
name|peerId
argument_list|,
name|rpc
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|TestUpdatableReplicationEndpoint
operator|.
name|hasCalledBack
argument_list|()
condition|)
block|{
synchronized|synchronized
init|(
name|TestUpdatableReplicationEndpoint
operator|.
name|class
init|)
block|{
name|TestUpdatableReplicationEndpoint
operator|.
name|class
operator|.
name|wait
argument_list|(
literal|2000L
argument_list|)
expr_stmt|;
block|}
block|}
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|TestUpdatableReplicationEndpoint
operator|.
name|hasCalledBack
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
class|class
name|TestUpdatableReplicationEndpoint
extends|extends
name|BaseReplicationEndpoint
block|{
specifier|private
specifier|static
name|boolean
name|calledBack
init|=
literal|false
decl_stmt|;
specifier|public
specifier|static
name|boolean
name|hasCalledBack
parameter_list|()
block|{
return|return
name|calledBack
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|peerConfigUpdated
parameter_list|(
name|ReplicationPeerConfig
name|rpc
parameter_list|)
block|{
name|calledBack
operator|=
literal|true
expr_stmt|;
name|notifyAll
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|doStart
parameter_list|()
block|{
name|notifyStarted
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|doStop
parameter_list|()
block|{
name|notifyStopped
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|UUID
name|getPeerUUID
parameter_list|()
block|{
return|return
name|UUID
operator|.
name|randomUUID
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|replicate
parameter_list|(
name|ReplicateContext
name|replicateContext
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
end_class

end_unit

