begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations  * under the License.  */
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
operator|.
name|example
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
name|HBaseTestingUtility
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
name|coprocessor
operator|.
name|CoprocessorHost
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
name|zookeeper
operator|.
name|ZKUtil
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
name|zookeeper
operator|.
name|ZooKeeperWatcher
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|ZooKeeper
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

begin_class
annotation|@
name|Category
argument_list|(
name|MediumTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestZooKeeperScanPolicyObserver
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
name|TestZooKeeperScanPolicyObserver
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|F
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fam"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|Q
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qual"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|R
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
argument_list|)
decl_stmt|;
comment|// @BeforeClass
specifier|public
specifier|static
name|void
name|setUpBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"HERE!!!!!!!!"
argument_list|)
expr_stmt|;
comment|// Test we can first start the ZK cluster by itself
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setStrings
argument_list|(
name|CoprocessorHost
operator|.
name|REGION_COPROCESSOR_CONF_KEY
argument_list|,
name|ZooKeeperScanPolicyObserver
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniZKCluster
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
block|}
comment|// @AfterClass
specifier|public
specifier|static
name|void
name|tearDownAfterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
comment|// @Ignore @Test
specifier|public
name|void
name|testScanPolicyObserver
parameter_list|()
throws|throws
name|Exception
block|{
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testScanPolicyObserver"
argument_list|)
decl_stmt|;
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|F
argument_list|)
operator|.
name|setMaxVersions
argument_list|(
literal|10
argument_list|)
operator|.
name|setTimeToLive
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|Table
name|t
init|=
operator|new
name|HTable
argument_list|(
operator|new
name|Configuration
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|long
name|now
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
name|ZooKeeperWatcher
name|zkw
init|=
operator|new
name|ZooKeeperWatcher
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|"test"
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|ZooKeeper
name|zk
init|=
name|zkw
operator|.
name|getRecoverableZooKeeper
argument_list|()
operator|.
name|getZooKeeper
argument_list|()
decl_stmt|;
name|ZKUtil
operator|.
name|createWithParents
argument_list|(
name|zkw
argument_list|,
name|ZooKeeperScanPolicyObserver
operator|.
name|node
argument_list|)
expr_stmt|;
comment|// let's say test last backup was 1h ago
comment|// using plain ZK here, because RecoverableZooKeeper add extra encoding to the data
name|zk
operator|.
name|setData
argument_list|(
name|ZooKeeperScanPolicyObserver
operator|.
name|node
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|now
operator|-
literal|3600
operator|*
literal|1000
argument_list|)
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Set time: "
operator|+
name|Bytes
operator|.
name|toLong
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|now
operator|-
literal|3600
operator|*
literal|1000
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// sleep for 1s to give the ZK change a chance to reach the watcher in the observer.
comment|// TODO: Better to wait for the data to be propagated
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
name|long
name|ts
init|=
name|now
operator|-
literal|2000
decl_stmt|;
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|R
argument_list|)
decl_stmt|;
name|p
operator|.
name|add
argument_list|(
name|F
argument_list|,
name|Q
argument_list|,
name|ts
argument_list|,
name|Q
argument_list|)
expr_stmt|;
name|t
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|p
operator|=
operator|new
name|Put
argument_list|(
name|R
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|F
argument_list|,
name|Q
argument_list|,
name|ts
operator|+
literal|1
argument_list|,
name|Q
argument_list|)
expr_stmt|;
name|t
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
comment|// these two should be expired but for the override
comment|// (their ts was 2s in the past)
name|Get
name|g
init|=
operator|new
name|Get
argument_list|(
name|R
argument_list|)
decl_stmt|;
name|g
operator|.
name|setMaxVersions
argument_list|(
literal|10
argument_list|)
expr_stmt|;
name|Result
name|r
init|=
name|t
operator|.
name|get
argument_list|(
name|g
argument_list|)
decl_stmt|;
comment|// still there?
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|r
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|flush
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|compact
argument_list|(
name|tableName
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|g
operator|=
operator|new
name|Get
argument_list|(
name|R
argument_list|)
expr_stmt|;
name|g
operator|.
name|setMaxVersions
argument_list|(
literal|10
argument_list|)
expr_stmt|;
name|r
operator|=
name|t
operator|.
name|get
argument_list|(
name|g
argument_list|)
expr_stmt|;
comment|// still there?
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|r
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|zk
operator|.
name|setData
argument_list|(
name|ZooKeeperScanPolicyObserver
operator|.
name|node
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|now
argument_list|)
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Set time: "
operator|+
name|now
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|compact
argument_list|(
name|tableName
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|g
operator|=
operator|new
name|Get
argument_list|(
name|R
argument_list|)
expr_stmt|;
name|g
operator|.
name|setMaxVersions
argument_list|(
literal|10
argument_list|)
expr_stmt|;
name|r
operator|=
name|t
operator|.
name|get
argument_list|(
name|g
argument_list|)
expr_stmt|;
comment|// should be gone now
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|r
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|t
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

