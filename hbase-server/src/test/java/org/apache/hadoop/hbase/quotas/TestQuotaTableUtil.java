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
name|quotas
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
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
name|HBaseAdmin
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
name|QuotaProtos
operator|.
name|Quotas
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
name|QuotaProtos
operator|.
name|Throttle
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
name|MasterTests
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

begin_comment
comment|/**  * Test the quota table helpers (e.g. CRUD operations)  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MasterTests
operator|.
name|class
block|,
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestQuotaTableUtil
block|{
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|getClass
argument_list|()
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
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setBoolean
argument_list|(
name|QuotaUtil
operator|.
name|QUOTA_CONF_KEY
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|QuotaCache
operator|.
name|REFRESH_CONF_KEY
argument_list|,
literal|2000
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.hstore.compactionThreshold"
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.regionserver.msginterval"
argument_list|,
literal|100
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.client.pause"
argument_list|,
literal|250
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_RETRIES_NUMBER
argument_list|,
literal|6
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setBoolean
argument_list|(
literal|"hbase.master.enabletable.roundrobin"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitTableAvailable
argument_list|(
name|QuotaTableUtil
operator|.
name|QUOTA_TABLE_NAME
operator|.
name|getName
argument_list|()
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
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTableQuotaUtil
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|TableName
name|table
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testTableQuotaUtilTable"
argument_list|)
decl_stmt|;
name|Quotas
name|quota
init|=
name|Quotas
operator|.
name|newBuilder
argument_list|()
operator|.
name|setThrottle
argument_list|(
name|Throttle
operator|.
name|newBuilder
argument_list|()
operator|.
name|setReqNum
argument_list|(
name|ProtobufUtil
operator|.
name|toTimedQuota
argument_list|(
literal|1000
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|,
name|QuotaScope
operator|.
name|MACHINE
argument_list|)
argument_list|)
operator|.
name|setWriteNum
argument_list|(
name|ProtobufUtil
operator|.
name|toTimedQuota
argument_list|(
literal|600
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|,
name|QuotaScope
operator|.
name|MACHINE
argument_list|)
argument_list|)
operator|.
name|setReadSize
argument_list|(
name|ProtobufUtil
operator|.
name|toTimedQuota
argument_list|(
literal|8192
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|,
name|QuotaScope
operator|.
name|MACHINE
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
comment|// Add user quota and verify it
name|QuotaUtil
operator|.
name|addTableQuota
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|table
argument_list|,
name|quota
argument_list|)
expr_stmt|;
name|Quotas
name|resQuota
init|=
name|QuotaUtil
operator|.
name|getTableQuota
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|table
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|quota
argument_list|,
name|resQuota
argument_list|)
expr_stmt|;
comment|// Remove user quota and verify it
name|QuotaUtil
operator|.
name|deleteTableQuota
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|table
argument_list|)
expr_stmt|;
name|resQuota
operator|=
name|QuotaUtil
operator|.
name|getTableQuota
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|table
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|null
argument_list|,
name|resQuota
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNamespaceQuotaUtil
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|String
name|namespace
init|=
literal|"testNamespaceQuotaUtilNS"
decl_stmt|;
name|Quotas
name|quota
init|=
name|Quotas
operator|.
name|newBuilder
argument_list|()
operator|.
name|setThrottle
argument_list|(
name|Throttle
operator|.
name|newBuilder
argument_list|()
operator|.
name|setReqNum
argument_list|(
name|ProtobufUtil
operator|.
name|toTimedQuota
argument_list|(
literal|1000
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|,
name|QuotaScope
operator|.
name|MACHINE
argument_list|)
argument_list|)
operator|.
name|setWriteNum
argument_list|(
name|ProtobufUtil
operator|.
name|toTimedQuota
argument_list|(
literal|600
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|,
name|QuotaScope
operator|.
name|MACHINE
argument_list|)
argument_list|)
operator|.
name|setReadSize
argument_list|(
name|ProtobufUtil
operator|.
name|toTimedQuota
argument_list|(
literal|8192
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|,
name|QuotaScope
operator|.
name|MACHINE
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
comment|// Add user quota and verify it
name|QuotaUtil
operator|.
name|addNamespaceQuota
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|namespace
argument_list|,
name|quota
argument_list|)
expr_stmt|;
name|Quotas
name|resQuota
init|=
name|QuotaUtil
operator|.
name|getNamespaceQuota
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|namespace
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|quota
argument_list|,
name|resQuota
argument_list|)
expr_stmt|;
comment|// Remove user quota and verify it
name|QuotaUtil
operator|.
name|deleteNamespaceQuota
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|namespace
argument_list|)
expr_stmt|;
name|resQuota
operator|=
name|QuotaUtil
operator|.
name|getNamespaceQuota
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|namespace
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|null
argument_list|,
name|resQuota
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUserQuotaUtil
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|TableName
name|table
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testUserQuotaUtilTable"
argument_list|)
decl_stmt|;
specifier|final
name|String
name|namespace
init|=
literal|"testNS"
decl_stmt|;
specifier|final
name|String
name|user
init|=
literal|"testUser"
decl_stmt|;
name|Quotas
name|quotaNamespace
init|=
name|Quotas
operator|.
name|newBuilder
argument_list|()
operator|.
name|setThrottle
argument_list|(
name|Throttle
operator|.
name|newBuilder
argument_list|()
operator|.
name|setReqNum
argument_list|(
name|ProtobufUtil
operator|.
name|toTimedQuota
argument_list|(
literal|50000
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|,
name|QuotaScope
operator|.
name|MACHINE
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|Quotas
name|quotaTable
init|=
name|Quotas
operator|.
name|newBuilder
argument_list|()
operator|.
name|setThrottle
argument_list|(
name|Throttle
operator|.
name|newBuilder
argument_list|()
operator|.
name|setReqNum
argument_list|(
name|ProtobufUtil
operator|.
name|toTimedQuota
argument_list|(
literal|1000
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|,
name|QuotaScope
operator|.
name|MACHINE
argument_list|)
argument_list|)
operator|.
name|setWriteNum
argument_list|(
name|ProtobufUtil
operator|.
name|toTimedQuota
argument_list|(
literal|600
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|,
name|QuotaScope
operator|.
name|MACHINE
argument_list|)
argument_list|)
operator|.
name|setReadSize
argument_list|(
name|ProtobufUtil
operator|.
name|toTimedQuota
argument_list|(
literal|10000
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|,
name|QuotaScope
operator|.
name|MACHINE
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|Quotas
name|quota
init|=
name|Quotas
operator|.
name|newBuilder
argument_list|()
operator|.
name|setThrottle
argument_list|(
name|Throttle
operator|.
name|newBuilder
argument_list|()
operator|.
name|setReqSize
argument_list|(
name|ProtobufUtil
operator|.
name|toTimedQuota
argument_list|(
literal|8192
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|,
name|QuotaScope
operator|.
name|MACHINE
argument_list|)
argument_list|)
operator|.
name|setWriteSize
argument_list|(
name|ProtobufUtil
operator|.
name|toTimedQuota
argument_list|(
literal|4096
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|,
name|QuotaScope
operator|.
name|MACHINE
argument_list|)
argument_list|)
operator|.
name|setReadNum
argument_list|(
name|ProtobufUtil
operator|.
name|toTimedQuota
argument_list|(
literal|1000
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|,
name|QuotaScope
operator|.
name|MACHINE
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
comment|// Add user global quota
name|QuotaUtil
operator|.
name|addUserQuota
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|user
argument_list|,
name|quota
argument_list|)
expr_stmt|;
name|Quotas
name|resQuota
init|=
name|QuotaUtil
operator|.
name|getUserQuota
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|user
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|quota
argument_list|,
name|resQuota
argument_list|)
expr_stmt|;
comment|// Add user quota for table
name|QuotaUtil
operator|.
name|addUserQuota
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|user
argument_list|,
name|table
argument_list|,
name|quotaTable
argument_list|)
expr_stmt|;
name|Quotas
name|resQuotaTable
init|=
name|QuotaUtil
operator|.
name|getUserQuota
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|user
argument_list|,
name|table
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|quotaTable
argument_list|,
name|resQuotaTable
argument_list|)
expr_stmt|;
comment|// Add user quota for namespace
name|QuotaUtil
operator|.
name|addUserQuota
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|user
argument_list|,
name|namespace
argument_list|,
name|quotaNamespace
argument_list|)
expr_stmt|;
name|Quotas
name|resQuotaNS
init|=
name|QuotaUtil
operator|.
name|getUserQuota
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|user
argument_list|,
name|namespace
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|quotaNamespace
argument_list|,
name|resQuotaNS
argument_list|)
expr_stmt|;
comment|// Delete user global quota
name|QuotaUtil
operator|.
name|deleteUserQuota
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|user
argument_list|)
expr_stmt|;
name|resQuota
operator|=
name|QuotaUtil
operator|.
name|getUserQuota
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|user
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|null
argument_list|,
name|resQuota
argument_list|)
expr_stmt|;
comment|// Delete user quota for table
name|QuotaUtil
operator|.
name|deleteUserQuota
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|user
argument_list|,
name|table
argument_list|)
expr_stmt|;
name|resQuotaTable
operator|=
name|QuotaUtil
operator|.
name|getUserQuota
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|user
argument_list|,
name|table
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|null
argument_list|,
name|resQuotaTable
argument_list|)
expr_stmt|;
comment|// Delete user quota for namespace
name|QuotaUtil
operator|.
name|deleteUserQuota
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|user
argument_list|,
name|namespace
argument_list|)
expr_stmt|;
name|resQuotaNS
operator|=
name|QuotaUtil
operator|.
name|getUserQuota
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|user
argument_list|,
name|namespace
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|null
argument_list|,
name|resQuotaNS
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

