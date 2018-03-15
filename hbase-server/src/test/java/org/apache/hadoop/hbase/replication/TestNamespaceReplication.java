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
name|assertArrayEquals
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
name|fail
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
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
name|HashSet
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
name|Set
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
name|HBaseClassTestRule
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
name|ColumnFamilyDescriptorBuilder
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
name|TableDescriptor
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
name|TableDescriptorBuilder
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
name|ClassRule
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
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestNamespaceReplication
extends|extends
name|TestReplicationBase
block|{
annotation|@
name|ClassRule
specifier|public
specifier|static
specifier|final
name|HBaseClassTestRule
name|CLASS_RULE
init|=
name|HBaseClassTestRule
operator|.
name|forClass
argument_list|(
name|TestNamespaceReplication
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestNamespaceReplication
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|String
name|ns1
init|=
literal|"ns1"
decl_stmt|;
specifier|private
specifier|static
name|String
name|ns2
init|=
literal|"ns2"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TableName
name|tabAName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"ns1:TA"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TableName
name|tabBName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"ns2:TB"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|f1Name
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f1"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|f2Name
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f2"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|val
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"myval"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|Connection
name|connection1
decl_stmt|;
specifier|private
specifier|static
name|Connection
name|connection2
decl_stmt|;
specifier|private
specifier|static
name|Admin
name|admin1
decl_stmt|;
specifier|private
specifier|static
name|Admin
name|admin2
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
name|admin1
operator|.
name|createNamespace
argument_list|(
name|NamespaceDescriptor
operator|.
name|create
argument_list|(
name|ns1
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|admin1
operator|.
name|createNamespace
argument_list|(
name|NamespaceDescriptor
operator|.
name|create
argument_list|(
name|ns2
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|admin2
operator|.
name|createNamespace
argument_list|(
name|NamespaceDescriptor
operator|.
name|create
argument_list|(
name|ns1
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|admin2
operator|.
name|createNamespace
argument_list|(
name|NamespaceDescriptor
operator|.
name|create
argument_list|(
name|ns2
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|TableDescriptorBuilder
name|builder
init|=
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|tabAName
argument_list|)
decl_stmt|;
name|builder
operator|.
name|setColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|f1Name
argument_list|)
operator|.
name|setScope
argument_list|(
name|HConstants
operator|.
name|REPLICATION_SCOPE_GLOBAL
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|f2Name
argument_list|)
operator|.
name|setScope
argument_list|(
name|HConstants
operator|.
name|REPLICATION_SCOPE_GLOBAL
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|TableDescriptor
name|tabA
init|=
name|builder
operator|.
name|build
argument_list|()
decl_stmt|;
name|admin1
operator|.
name|createTable
argument_list|(
name|tabA
argument_list|)
expr_stmt|;
name|admin2
operator|.
name|createTable
argument_list|(
name|tabA
argument_list|)
expr_stmt|;
name|builder
operator|=
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|tabBName
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|f1Name
argument_list|)
operator|.
name|setScope
argument_list|(
name|HConstants
operator|.
name|REPLICATION_SCOPE_GLOBAL
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|f2Name
argument_list|)
operator|.
name|setScope
argument_list|(
name|HConstants
operator|.
name|REPLICATION_SCOPE_GLOBAL
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|TableDescriptor
name|tabB
init|=
name|builder
operator|.
name|build
argument_list|()
decl_stmt|;
name|admin1
operator|.
name|createTable
argument_list|(
name|tabB
argument_list|)
expr_stmt|;
name|admin2
operator|.
name|createTable
argument_list|(
name|tabB
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
name|disableTable
argument_list|(
name|tabAName
argument_list|)
expr_stmt|;
name|admin1
operator|.
name|deleteTable
argument_list|(
name|tabAName
argument_list|)
expr_stmt|;
name|admin1
operator|.
name|disableTable
argument_list|(
name|tabBName
argument_list|)
expr_stmt|;
name|admin1
operator|.
name|deleteTable
argument_list|(
name|tabBName
argument_list|)
expr_stmt|;
name|admin2
operator|.
name|disableTable
argument_list|(
name|tabAName
argument_list|)
expr_stmt|;
name|admin2
operator|.
name|deleteTable
argument_list|(
name|tabAName
argument_list|)
expr_stmt|;
name|admin2
operator|.
name|disableTable
argument_list|(
name|tabBName
argument_list|)
expr_stmt|;
name|admin2
operator|.
name|deleteTable
argument_list|(
name|tabBName
argument_list|)
expr_stmt|;
name|admin1
operator|.
name|deleteNamespace
argument_list|(
name|ns1
argument_list|)
expr_stmt|;
name|admin1
operator|.
name|deleteNamespace
argument_list|(
name|ns2
argument_list|)
expr_stmt|;
name|admin2
operator|.
name|deleteNamespace
argument_list|(
name|ns1
argument_list|)
expr_stmt|;
name|admin2
operator|.
name|deleteNamespace
argument_list|(
name|ns2
argument_list|)
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
specifier|public
name|void
name|testNamespaceReplication
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|peerId
init|=
literal|"2"
decl_stmt|;
name|Table
name|htab1A
init|=
name|connection1
operator|.
name|getTable
argument_list|(
name|tabAName
argument_list|)
decl_stmt|;
name|Table
name|htab2A
init|=
name|connection2
operator|.
name|getTable
argument_list|(
name|tabAName
argument_list|)
decl_stmt|;
name|Table
name|htab1B
init|=
name|connection1
operator|.
name|getTable
argument_list|(
name|tabBName
argument_list|)
decl_stmt|;
name|Table
name|htab2B
init|=
name|connection2
operator|.
name|getTable
argument_list|(
name|tabBName
argument_list|)
decl_stmt|;
name|ReplicationPeerConfig
name|rpc
init|=
name|admin1
operator|.
name|getReplicationPeerConfig
argument_list|(
name|peerId
argument_list|)
decl_stmt|;
name|admin1
operator|.
name|updateReplicationPeerConfig
argument_list|(
name|peerId
argument_list|,
name|ReplicationPeerConfig
operator|.
name|newBuilder
argument_list|(
name|rpc
argument_list|)
operator|.
name|setReplicateAllUserTables
argument_list|(
literal|false
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
comment|// add ns1 to peer config which replicate to cluster2
name|rpc
operator|=
name|admin1
operator|.
name|getReplicationPeerConfig
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|namespaces
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|namespaces
operator|.
name|add
argument_list|(
name|ns1
argument_list|)
expr_stmt|;
name|admin1
operator|.
name|updateReplicationPeerConfig
argument_list|(
name|peerId
argument_list|,
name|ReplicationPeerConfig
operator|.
name|newBuilder
argument_list|(
name|rpc
argument_list|)
operator|.
name|setNamespaces
argument_list|(
name|namespaces
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"update peer config"
argument_list|)
expr_stmt|;
comment|// Table A can be replicated to cluster2
name|put
argument_list|(
name|htab1A
argument_list|,
name|row
argument_list|,
name|f1Name
argument_list|,
name|f2Name
argument_list|)
expr_stmt|;
name|ensureRowExisted
argument_list|(
name|htab2A
argument_list|,
name|row
argument_list|,
name|f1Name
argument_list|,
name|f2Name
argument_list|)
expr_stmt|;
name|delete
argument_list|(
name|htab1A
argument_list|,
name|row
argument_list|,
name|f1Name
argument_list|,
name|f2Name
argument_list|)
expr_stmt|;
name|ensureRowNotExisted
argument_list|(
name|htab2A
argument_list|,
name|row
argument_list|,
name|f1Name
argument_list|,
name|f2Name
argument_list|)
expr_stmt|;
comment|// Table B can not be replicated to cluster2
name|put
argument_list|(
name|htab1B
argument_list|,
name|row
argument_list|,
name|f1Name
argument_list|,
name|f2Name
argument_list|)
expr_stmt|;
name|ensureRowNotExisted
argument_list|(
name|htab2B
argument_list|,
name|row
argument_list|,
name|f1Name
argument_list|,
name|f2Name
argument_list|)
expr_stmt|;
comment|// add ns1:TA => 'f1' and ns2 to peer config which replicate to cluster2
name|rpc
operator|=
name|admin1
operator|.
name|getReplicationPeerConfig
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
name|namespaces
operator|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
expr_stmt|;
name|namespaces
operator|.
name|add
argument_list|(
name|ns2
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|TableName
argument_list|,
name|List
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
name|tabAName
argument_list|,
operator|new
name|ArrayList
argument_list|<>
argument_list|()
argument_list|)
expr_stmt|;
name|tableCfs
operator|.
name|get
argument_list|(
name|tabAName
argument_list|)
operator|.
name|add
argument_list|(
literal|"f1"
argument_list|)
expr_stmt|;
name|admin1
operator|.
name|updateReplicationPeerConfig
argument_list|(
name|peerId
argument_list|,
name|ReplicationPeerConfig
operator|.
name|newBuilder
argument_list|(
name|rpc
argument_list|)
operator|.
name|setNamespaces
argument_list|(
name|namespaces
argument_list|)
operator|.
name|setTableCFsMap
argument_list|(
name|tableCfs
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"update peer config"
argument_list|)
expr_stmt|;
comment|// Only family f1 of Table A can replicated to cluster2
name|put
argument_list|(
name|htab1A
argument_list|,
name|row
argument_list|,
name|f1Name
argument_list|,
name|f2Name
argument_list|)
expr_stmt|;
name|ensureRowExisted
argument_list|(
name|htab2A
argument_list|,
name|row
argument_list|,
name|f1Name
argument_list|)
expr_stmt|;
name|delete
argument_list|(
name|htab1A
argument_list|,
name|row
argument_list|,
name|f1Name
argument_list|,
name|f2Name
argument_list|)
expr_stmt|;
name|ensureRowNotExisted
argument_list|(
name|htab2A
argument_list|,
name|row
argument_list|,
name|f1Name
argument_list|)
expr_stmt|;
comment|// All cfs of table B can replicated to cluster2
name|put
argument_list|(
name|htab1B
argument_list|,
name|row
argument_list|,
name|f1Name
argument_list|,
name|f2Name
argument_list|)
expr_stmt|;
name|ensureRowExisted
argument_list|(
name|htab2B
argument_list|,
name|row
argument_list|,
name|f1Name
argument_list|,
name|f2Name
argument_list|)
expr_stmt|;
name|delete
argument_list|(
name|htab1B
argument_list|,
name|row
argument_list|,
name|f1Name
argument_list|,
name|f2Name
argument_list|)
expr_stmt|;
name|ensureRowNotExisted
argument_list|(
name|htab2B
argument_list|,
name|row
argument_list|,
name|f1Name
argument_list|,
name|f2Name
argument_list|)
expr_stmt|;
name|admin1
operator|.
name|removeReplicationPeer
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|put
parameter_list|(
name|Table
name|source
parameter_list|,
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
modifier|...
name|families
parameter_list|)
throws|throws
name|Exception
block|{
for|for
control|(
name|byte
index|[]
name|fam
range|:
name|families
control|)
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|fam
argument_list|,
name|row
argument_list|,
name|val
argument_list|)
expr_stmt|;
name|source
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|delete
parameter_list|(
name|Table
name|source
parameter_list|,
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
modifier|...
name|families
parameter_list|)
throws|throws
name|Exception
block|{
for|for
control|(
name|byte
index|[]
name|fam
range|:
name|families
control|)
block|{
name|Delete
name|del
init|=
operator|new
name|Delete
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|del
operator|.
name|addFamily
argument_list|(
name|fam
argument_list|)
expr_stmt|;
name|source
operator|.
name|delete
argument_list|(
name|del
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|ensureRowExisted
parameter_list|(
name|Table
name|target
parameter_list|,
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
modifier|...
name|families
parameter_list|)
throws|throws
name|Exception
block|{
for|for
control|(
name|byte
index|[]
name|fam
range|:
name|families
control|)
block|{
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|get
operator|.
name|addFamily
argument_list|(
name|fam
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|NB_RETRIES
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|==
name|NB_RETRIES
operator|-
literal|1
condition|)
block|{
name|fail
argument_list|(
literal|"Waited too much time for put replication"
argument_list|)
expr_stmt|;
block|}
name|Result
name|res
init|=
name|target
operator|.
name|get
argument_list|(
name|get
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
name|LOG
operator|.
name|info
argument_list|(
literal|"Row not available"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|res
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|val
argument_list|,
name|res
operator|.
name|value
argument_list|()
argument_list|)
expr_stmt|;
break|break;
block|}
name|Thread
operator|.
name|sleep
argument_list|(
name|SLEEP_TIME
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|ensureRowNotExisted
parameter_list|(
name|Table
name|target
parameter_list|,
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
modifier|...
name|families
parameter_list|)
throws|throws
name|Exception
block|{
for|for
control|(
name|byte
index|[]
name|fam
range|:
name|families
control|)
block|{
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|get
operator|.
name|addFamily
argument_list|(
name|fam
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|NB_RETRIES
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|==
name|NB_RETRIES
operator|-
literal|1
condition|)
block|{
name|fail
argument_list|(
literal|"Waited too much time for delete replication"
argument_list|)
expr_stmt|;
block|}
name|Result
name|res
init|=
name|target
operator|.
name|get
argument_list|(
name|get
argument_list|)
decl_stmt|;
if|if
condition|(
name|res
operator|.
name|size
argument_list|()
operator|>=
literal|1
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Row not deleted"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
break|break;
block|}
name|Thread
operator|.
name|sleep
argument_list|(
name|SLEEP_TIME
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

