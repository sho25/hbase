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
name|RetriesExhaustedWithDetailsException
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
name|security
operator|.
name|User
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
name|EnvironmentEdgeManagerTestHelper
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
name|IncrementingEnvironmentEdge
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
name|JVMClusterUtil
operator|.
name|RegionServerThread
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

begin_comment
comment|/**  * minicluster tests that validate that quota  entries are properly set in the quota table  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|ClientTests
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
name|TestQuotaAdmin
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
name|TestQuotaAdmin
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
name|testSimpleScan
parameter_list|()
throws|throws
name|Exception
block|{
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
name|String
name|userName
init|=
name|User
operator|.
name|getCurrent
argument_list|()
operator|.
name|getShortName
argument_list|()
decl_stmt|;
name|admin
operator|.
name|setQuota
argument_list|(
name|QuotaSettingsFactory
operator|.
name|throttleUser
argument_list|(
name|userName
argument_list|,
name|ThrottleType
operator|.
name|REQUEST_NUMBER
argument_list|,
literal|6
argument_list|,
name|TimeUnit
operator|.
name|MINUTES
argument_list|)
argument_list|)
expr_stmt|;
name|admin
operator|.
name|setQuota
argument_list|(
name|QuotaSettingsFactory
operator|.
name|bypassGlobals
argument_list|(
name|userName
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|QuotaRetriever
name|scanner
init|=
name|QuotaRetriever
operator|.
name|open
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|int
name|countThrottle
init|=
literal|0
decl_stmt|;
name|int
name|countGlobalBypass
init|=
literal|0
decl_stmt|;
for|for
control|(
name|QuotaSettings
name|settings
range|:
name|scanner
control|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|settings
argument_list|)
expr_stmt|;
switch|switch
condition|(
name|settings
operator|.
name|getQuotaType
argument_list|()
condition|)
block|{
case|case
name|THROTTLE
case|:
name|ThrottleSettings
name|throttle
init|=
operator|(
name|ThrottleSettings
operator|)
name|settings
decl_stmt|;
name|assertEquals
argument_list|(
name|userName
argument_list|,
name|throttle
operator|.
name|getUserName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|null
argument_list|,
name|throttle
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|null
argument_list|,
name|throttle
operator|.
name|getNamespace
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|6
argument_list|,
name|throttle
operator|.
name|getSoftLimit
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|TimeUnit
operator|.
name|MINUTES
argument_list|,
name|throttle
operator|.
name|getTimeUnit
argument_list|()
argument_list|)
expr_stmt|;
name|countThrottle
operator|++
expr_stmt|;
break|break;
case|case
name|GLOBAL_BYPASS
case|:
name|countGlobalBypass
operator|++
expr_stmt|;
break|break;
default|default:
name|fail
argument_list|(
literal|"unexpected settings type: "
operator|+
name|settings
operator|.
name|getQuotaType
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|countThrottle
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|countGlobalBypass
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|admin
operator|.
name|setQuota
argument_list|(
name|QuotaSettingsFactory
operator|.
name|unthrottleUser
argument_list|(
name|userName
argument_list|)
argument_list|)
expr_stmt|;
name|assertNumResults
argument_list|(
literal|1
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|admin
operator|.
name|setQuota
argument_list|(
name|QuotaSettingsFactory
operator|.
name|bypassGlobals
argument_list|(
name|userName
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertNumResults
argument_list|(
literal|0
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testQuotaRetrieverFilter
parameter_list|()
throws|throws
name|Exception
block|{
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
name|TableName
index|[]
name|tables
init|=
operator|new
name|TableName
index|[]
block|{
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"T0"
argument_list|)
block|,
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"T01"
argument_list|)
block|,
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"NS0:T2"
argument_list|)
block|,     }
decl_stmt|;
name|String
index|[]
name|namespaces
init|=
operator|new
name|String
index|[]
block|{
literal|"NS0"
block|,
literal|"NS01"
block|,
literal|"NS2"
block|}
decl_stmt|;
name|String
index|[]
name|users
init|=
operator|new
name|String
index|[]
block|{
literal|"User0"
block|,
literal|"User01"
block|,
literal|"User2"
block|}
decl_stmt|;
for|for
control|(
name|String
name|user
range|:
name|users
control|)
block|{
name|admin
operator|.
name|setQuota
argument_list|(
name|QuotaSettingsFactory
operator|.
name|throttleUser
argument_list|(
name|user
argument_list|,
name|ThrottleType
operator|.
name|REQUEST_NUMBER
argument_list|,
literal|1
argument_list|,
name|TimeUnit
operator|.
name|MINUTES
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|TableName
name|table
range|:
name|tables
control|)
block|{
name|admin
operator|.
name|setQuota
argument_list|(
name|QuotaSettingsFactory
operator|.
name|throttleUser
argument_list|(
name|user
argument_list|,
name|table
argument_list|,
name|ThrottleType
operator|.
name|REQUEST_NUMBER
argument_list|,
literal|2
argument_list|,
name|TimeUnit
operator|.
name|MINUTES
argument_list|)
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|String
name|ns
range|:
name|namespaces
control|)
block|{
name|admin
operator|.
name|setQuota
argument_list|(
name|QuotaSettingsFactory
operator|.
name|throttleUser
argument_list|(
name|user
argument_list|,
name|ns
argument_list|,
name|ThrottleType
operator|.
name|REQUEST_NUMBER
argument_list|,
literal|3
argument_list|,
name|TimeUnit
operator|.
name|MINUTES
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|assertNumResults
argument_list|(
literal|21
argument_list|,
literal|null
argument_list|)
expr_stmt|;
for|for
control|(
name|TableName
name|table
range|:
name|tables
control|)
block|{
name|admin
operator|.
name|setQuota
argument_list|(
name|QuotaSettingsFactory
operator|.
name|throttleTable
argument_list|(
name|table
argument_list|,
name|ThrottleType
operator|.
name|REQUEST_NUMBER
argument_list|,
literal|4
argument_list|,
name|TimeUnit
operator|.
name|MINUTES
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertNumResults
argument_list|(
literal|24
argument_list|,
literal|null
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|ns
range|:
name|namespaces
control|)
block|{
name|admin
operator|.
name|setQuota
argument_list|(
name|QuotaSettingsFactory
operator|.
name|throttleNamespace
argument_list|(
name|ns
argument_list|,
name|ThrottleType
operator|.
name|REQUEST_NUMBER
argument_list|,
literal|5
argument_list|,
name|TimeUnit
operator|.
name|MINUTES
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertNumResults
argument_list|(
literal|27
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertNumResults
argument_list|(
literal|7
argument_list|,
operator|new
name|QuotaFilter
argument_list|()
operator|.
name|setUserFilter
argument_list|(
literal|"User0"
argument_list|)
argument_list|)
expr_stmt|;
name|assertNumResults
argument_list|(
literal|0
argument_list|,
operator|new
name|QuotaFilter
argument_list|()
operator|.
name|setUserFilter
argument_list|(
literal|"User"
argument_list|)
argument_list|)
expr_stmt|;
name|assertNumResults
argument_list|(
literal|21
argument_list|,
operator|new
name|QuotaFilter
argument_list|()
operator|.
name|setUserFilter
argument_list|(
literal|"User.*"
argument_list|)
argument_list|)
expr_stmt|;
name|assertNumResults
argument_list|(
literal|3
argument_list|,
operator|new
name|QuotaFilter
argument_list|()
operator|.
name|setUserFilter
argument_list|(
literal|"User.*"
argument_list|)
operator|.
name|setTableFilter
argument_list|(
literal|"T0"
argument_list|)
argument_list|)
expr_stmt|;
name|assertNumResults
argument_list|(
literal|3
argument_list|,
operator|new
name|QuotaFilter
argument_list|()
operator|.
name|setUserFilter
argument_list|(
literal|"User.*"
argument_list|)
operator|.
name|setTableFilter
argument_list|(
literal|"NS.*"
argument_list|)
argument_list|)
expr_stmt|;
name|assertNumResults
argument_list|(
literal|0
argument_list|,
operator|new
name|QuotaFilter
argument_list|()
operator|.
name|setUserFilter
argument_list|(
literal|"User.*"
argument_list|)
operator|.
name|setTableFilter
argument_list|(
literal|"T"
argument_list|)
argument_list|)
expr_stmt|;
name|assertNumResults
argument_list|(
literal|6
argument_list|,
operator|new
name|QuotaFilter
argument_list|()
operator|.
name|setUserFilter
argument_list|(
literal|"User.*"
argument_list|)
operator|.
name|setTableFilter
argument_list|(
literal|"T.*"
argument_list|)
argument_list|)
expr_stmt|;
name|assertNumResults
argument_list|(
literal|3
argument_list|,
operator|new
name|QuotaFilter
argument_list|()
operator|.
name|setUserFilter
argument_list|(
literal|"User.*"
argument_list|)
operator|.
name|setNamespaceFilter
argument_list|(
literal|"NS0"
argument_list|)
argument_list|)
expr_stmt|;
name|assertNumResults
argument_list|(
literal|0
argument_list|,
operator|new
name|QuotaFilter
argument_list|()
operator|.
name|setUserFilter
argument_list|(
literal|"User.*"
argument_list|)
operator|.
name|setNamespaceFilter
argument_list|(
literal|"NS"
argument_list|)
argument_list|)
expr_stmt|;
name|assertNumResults
argument_list|(
literal|9
argument_list|,
operator|new
name|QuotaFilter
argument_list|()
operator|.
name|setUserFilter
argument_list|(
literal|"User.*"
argument_list|)
operator|.
name|setNamespaceFilter
argument_list|(
literal|"NS.*"
argument_list|)
argument_list|)
expr_stmt|;
name|assertNumResults
argument_list|(
literal|6
argument_list|,
operator|new
name|QuotaFilter
argument_list|()
operator|.
name|setUserFilter
argument_list|(
literal|"User.*"
argument_list|)
operator|.
name|setTableFilter
argument_list|(
literal|"T0"
argument_list|)
operator|.
name|setNamespaceFilter
argument_list|(
literal|"NS0"
argument_list|)
argument_list|)
expr_stmt|;
name|assertNumResults
argument_list|(
literal|1
argument_list|,
operator|new
name|QuotaFilter
argument_list|()
operator|.
name|setTableFilter
argument_list|(
literal|"T0"
argument_list|)
argument_list|)
expr_stmt|;
name|assertNumResults
argument_list|(
literal|0
argument_list|,
operator|new
name|QuotaFilter
argument_list|()
operator|.
name|setTableFilter
argument_list|(
literal|"T"
argument_list|)
argument_list|)
expr_stmt|;
name|assertNumResults
argument_list|(
literal|2
argument_list|,
operator|new
name|QuotaFilter
argument_list|()
operator|.
name|setTableFilter
argument_list|(
literal|"T.*"
argument_list|)
argument_list|)
expr_stmt|;
name|assertNumResults
argument_list|(
literal|3
argument_list|,
operator|new
name|QuotaFilter
argument_list|()
operator|.
name|setTableFilter
argument_list|(
literal|".*T.*"
argument_list|)
argument_list|)
expr_stmt|;
name|assertNumResults
argument_list|(
literal|1
argument_list|,
operator|new
name|QuotaFilter
argument_list|()
operator|.
name|setNamespaceFilter
argument_list|(
literal|"NS0"
argument_list|)
argument_list|)
expr_stmt|;
name|assertNumResults
argument_list|(
literal|0
argument_list|,
operator|new
name|QuotaFilter
argument_list|()
operator|.
name|setNamespaceFilter
argument_list|(
literal|"NS"
argument_list|)
argument_list|)
expr_stmt|;
name|assertNumResults
argument_list|(
literal|3
argument_list|,
operator|new
name|QuotaFilter
argument_list|()
operator|.
name|setNamespaceFilter
argument_list|(
literal|"NS.*"
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|user
range|:
name|users
control|)
block|{
name|admin
operator|.
name|setQuota
argument_list|(
name|QuotaSettingsFactory
operator|.
name|unthrottleUser
argument_list|(
name|user
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|TableName
name|table
range|:
name|tables
control|)
block|{
name|admin
operator|.
name|setQuota
argument_list|(
name|QuotaSettingsFactory
operator|.
name|unthrottleUser
argument_list|(
name|user
argument_list|,
name|table
argument_list|)
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|String
name|ns
range|:
name|namespaces
control|)
block|{
name|admin
operator|.
name|setQuota
argument_list|(
name|QuotaSettingsFactory
operator|.
name|unthrottleUser
argument_list|(
name|user
argument_list|,
name|ns
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|assertNumResults
argument_list|(
literal|6
argument_list|,
literal|null
argument_list|)
expr_stmt|;
for|for
control|(
name|TableName
name|table
range|:
name|tables
control|)
block|{
name|admin
operator|.
name|setQuota
argument_list|(
name|QuotaSettingsFactory
operator|.
name|unthrottleTable
argument_list|(
name|table
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertNumResults
argument_list|(
literal|3
argument_list|,
literal|null
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|ns
range|:
name|namespaces
control|)
block|{
name|admin
operator|.
name|setQuota
argument_list|(
name|QuotaSettingsFactory
operator|.
name|unthrottleNamespace
argument_list|(
name|ns
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertNumResults
argument_list|(
literal|0
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|assertNumResults
parameter_list|(
name|int
name|expected
parameter_list|,
specifier|final
name|QuotaFilter
name|filter
parameter_list|)
throws|throws
name|Exception
block|{
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|countResults
argument_list|(
name|filter
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|int
name|countResults
parameter_list|(
specifier|final
name|QuotaFilter
name|filter
parameter_list|)
throws|throws
name|Exception
block|{
name|QuotaRetriever
name|scanner
init|=
name|QuotaRetriever
operator|.
name|open
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|filter
argument_list|)
decl_stmt|;
try|try
block|{
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|QuotaSettings
name|settings
range|:
name|scanner
control|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|count
operator|++
expr_stmt|;
block|}
return|return
name|count
return|;
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
block|}
end_class

end_unit

