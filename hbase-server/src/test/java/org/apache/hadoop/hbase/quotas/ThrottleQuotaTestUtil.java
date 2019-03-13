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
name|Objects
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
name|JVMClusterUtil
operator|.
name|RegionServerThread
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
name|ManualEnvironmentEdge
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
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|ThrottleQuotaTestUtil
block|{
specifier|private
specifier|final
specifier|static
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|ThrottleQuotaTestUtil
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|ManualEnvironmentEdge
name|envEdge
init|=
operator|new
name|ManualEnvironmentEdge
argument_list|()
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|int
name|REFRESH_TIME
init|=
literal|30
operator|*
literal|60000
decl_stmt|;
static|static
block|{
name|envEdge
operator|.
name|setValue
argument_list|(
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
argument_list|)
expr_stmt|;
name|EnvironmentEdgeManagerTestHelper
operator|.
name|injectEdge
argument_list|(
name|envEdge
argument_list|)
expr_stmt|;
block|}
specifier|private
name|ThrottleQuotaTestUtil
parameter_list|()
block|{
comment|// Hide utility class constructor
name|LOG
operator|.
name|debug
argument_list|(
literal|"Call constructor of ThrottleQuotaTestUtil"
argument_list|)
expr_stmt|;
block|}
specifier|static
name|int
name|doPuts
parameter_list|(
name|int
name|maxOps
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
specifier|final
name|Table
modifier|...
name|tables
parameter_list|)
block|{
return|return
name|doPuts
argument_list|(
name|maxOps
argument_list|,
operator|-
literal|1
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|tables
argument_list|)
return|;
block|}
specifier|static
name|int
name|doPuts
parameter_list|(
name|int
name|maxOps
parameter_list|,
name|int
name|valueSize
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
specifier|final
name|Table
modifier|...
name|tables
parameter_list|)
block|{
name|int
name|count
init|=
literal|0
decl_stmt|;
try|try
block|{
while|while
condition|(
name|count
operator|<
name|maxOps
condition|)
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row-"
operator|+
name|count
argument_list|)
argument_list|)
decl_stmt|;
name|byte
index|[]
name|value
decl_stmt|;
if|if
condition|(
name|valueSize
operator|<
literal|0
condition|)
block|{
name|value
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"data-"
operator|+
name|count
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|value
operator|=
name|generateValue
argument_list|(
name|valueSize
argument_list|)
expr_stmt|;
block|}
name|put
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|,
name|value
argument_list|)
expr_stmt|;
for|for
control|(
specifier|final
name|Table
name|table
range|:
name|tables
control|)
block|{
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
name|count
operator|+=
name|tables
operator|.
name|length
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"put failed after nRetries="
operator|+
name|count
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
name|count
return|;
block|}
specifier|private
specifier|static
name|byte
index|[]
name|generateValue
parameter_list|(
name|int
name|valueSize
parameter_list|)
block|{
name|byte
index|[]
name|bytes
init|=
operator|new
name|byte
index|[
name|valueSize
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|valueSize
condition|;
name|i
operator|++
control|)
block|{
name|bytes
index|[
name|i
index|]
operator|=
literal|'a'
expr_stmt|;
block|}
return|return
name|bytes
return|;
block|}
specifier|static
name|long
name|doGets
parameter_list|(
name|int
name|maxOps
parameter_list|,
specifier|final
name|Table
modifier|...
name|tables
parameter_list|)
block|{
name|int
name|count
init|=
literal|0
decl_stmt|;
try|try
block|{
while|while
condition|(
name|count
operator|<
name|maxOps
condition|)
block|{
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row-"
operator|+
name|count
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
specifier|final
name|Table
name|table
range|:
name|tables
control|)
block|{
name|table
operator|.
name|get
argument_list|(
name|get
argument_list|)
expr_stmt|;
block|}
name|count
operator|+=
name|tables
operator|.
name|length
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"get failed after nRetries="
operator|+
name|count
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
name|count
return|;
block|}
specifier|static
name|void
name|triggerUserCacheRefresh
parameter_list|(
name|HBaseTestingUtility
name|testUtil
parameter_list|,
name|boolean
name|bypass
parameter_list|,
name|TableName
modifier|...
name|tables
parameter_list|)
throws|throws
name|Exception
block|{
name|triggerCacheRefresh
argument_list|(
name|testUtil
argument_list|,
name|bypass
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
name|tables
argument_list|)
expr_stmt|;
block|}
specifier|static
name|void
name|triggerTableCacheRefresh
parameter_list|(
name|HBaseTestingUtility
name|testUtil
parameter_list|,
name|boolean
name|bypass
parameter_list|,
name|TableName
modifier|...
name|tables
parameter_list|)
throws|throws
name|Exception
block|{
name|triggerCacheRefresh
argument_list|(
name|testUtil
argument_list|,
name|bypass
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
name|tables
argument_list|)
expr_stmt|;
block|}
specifier|static
name|void
name|triggerNamespaceCacheRefresh
parameter_list|(
name|HBaseTestingUtility
name|testUtil
parameter_list|,
name|boolean
name|bypass
parameter_list|,
name|TableName
modifier|...
name|tables
parameter_list|)
throws|throws
name|Exception
block|{
name|triggerCacheRefresh
argument_list|(
name|testUtil
argument_list|,
name|bypass
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
name|tables
argument_list|)
expr_stmt|;
block|}
specifier|static
name|void
name|triggerRegionServerCacheRefresh
parameter_list|(
name|HBaseTestingUtility
name|testUtil
parameter_list|,
name|boolean
name|bypass
parameter_list|)
throws|throws
name|Exception
block|{
name|triggerCacheRefresh
argument_list|(
name|testUtil
argument_list|,
name|bypass
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|static
name|void
name|triggerExceedThrottleQuotaCacheRefresh
parameter_list|(
name|HBaseTestingUtility
name|testUtil
parameter_list|,
name|boolean
name|exceedEnabled
parameter_list|)
throws|throws
name|Exception
block|{
name|triggerCacheRefresh
argument_list|(
name|testUtil
argument_list|,
name|exceedEnabled
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|triggerCacheRefresh
parameter_list|(
name|HBaseTestingUtility
name|testUtil
parameter_list|,
name|boolean
name|bypass
parameter_list|,
name|boolean
name|userLimiter
parameter_list|,
name|boolean
name|tableLimiter
parameter_list|,
name|boolean
name|nsLimiter
parameter_list|,
name|boolean
name|rsLimiter
parameter_list|,
name|boolean
name|exceedThrottleQuota
parameter_list|,
specifier|final
name|TableName
modifier|...
name|tables
parameter_list|)
throws|throws
name|Exception
block|{
name|envEdge
operator|.
name|incValue
argument_list|(
literal|2
operator|*
name|REFRESH_TIME
argument_list|)
expr_stmt|;
for|for
control|(
name|RegionServerThread
name|rst
range|:
name|testUtil
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegionServerThreads
argument_list|()
control|)
block|{
name|RegionServerRpcQuotaManager
name|quotaManager
init|=
name|rst
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getRegionServerRpcQuotaManager
argument_list|()
decl_stmt|;
name|QuotaCache
name|quotaCache
init|=
name|quotaManager
operator|.
name|getQuotaCache
argument_list|()
decl_stmt|;
name|quotaCache
operator|.
name|triggerCacheRefresh
argument_list|()
expr_stmt|;
comment|// sleep for cache update
name|Thread
operator|.
name|sleep
argument_list|(
literal|250
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
name|quotaCache
operator|.
name|getTableLimiter
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
name|boolean
name|isUpdated
init|=
literal|false
decl_stmt|;
while|while
condition|(
operator|!
name|isUpdated
condition|)
block|{
name|quotaCache
operator|.
name|triggerCacheRefresh
argument_list|()
expr_stmt|;
name|isUpdated
operator|=
literal|true
expr_stmt|;
for|for
control|(
name|TableName
name|table
range|:
name|tables
control|)
block|{
name|boolean
name|isBypass
init|=
literal|true
decl_stmt|;
if|if
condition|(
name|userLimiter
condition|)
block|{
name|isBypass
operator|=
name|quotaCache
operator|.
name|getUserLimiter
argument_list|(
name|User
operator|.
name|getCurrent
argument_list|()
operator|.
name|getUGI
argument_list|()
argument_list|,
name|table
argument_list|)
operator|.
name|isBypass
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|tableLimiter
condition|)
block|{
name|isBypass
operator|&=
name|quotaCache
operator|.
name|getTableLimiter
argument_list|(
name|table
argument_list|)
operator|.
name|isBypass
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|nsLimiter
condition|)
block|{
name|isBypass
operator|&=
name|quotaCache
operator|.
name|getNamespaceLimiter
argument_list|(
name|table
operator|.
name|getNamespaceAsString
argument_list|()
argument_list|)
operator|.
name|isBypass
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|isBypass
operator|!=
name|bypass
condition|)
block|{
name|envEdge
operator|.
name|incValue
argument_list|(
literal|100
argument_list|)
expr_stmt|;
name|isUpdated
operator|=
literal|false
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
name|rsLimiter
condition|)
block|{
name|boolean
name|rsIsBypass
init|=
name|quotaCache
operator|.
name|getRegionServerQuotaLimiter
argument_list|(
name|QuotaTableUtil
operator|.
name|QUOTA_REGION_SERVER_ROW_KEY
argument_list|)
operator|.
name|isBypass
argument_list|()
decl_stmt|;
if|if
condition|(
name|rsIsBypass
operator|!=
name|bypass
condition|)
block|{
name|envEdge
operator|.
name|incValue
argument_list|(
literal|100
argument_list|)
expr_stmt|;
name|isUpdated
operator|=
literal|false
expr_stmt|;
block|}
block|}
if|if
condition|(
name|exceedThrottleQuota
condition|)
block|{
if|if
condition|(
name|quotaCache
operator|.
name|isExceedThrottleQuotaEnabled
argument_list|()
operator|!=
name|bypass
condition|)
block|{
name|envEdge
operator|.
name|incValue
argument_list|(
literal|100
argument_list|)
expr_stmt|;
name|isUpdated
operator|=
literal|false
expr_stmt|;
block|}
block|}
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"QuotaCache"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|Objects
operator|.
name|toString
argument_list|(
name|quotaCache
operator|.
name|getNamespaceQuotaCache
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|Objects
operator|.
name|toString
argument_list|(
name|quotaCache
operator|.
name|getTableQuotaCache
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|Objects
operator|.
name|toString
argument_list|(
name|quotaCache
operator|.
name|getUserQuotaCache
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|Objects
operator|.
name|toString
argument_list|(
name|quotaCache
operator|.
name|getRegionServerQuotaCache
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|static
name|void
name|waitMinuteQuota
parameter_list|()
block|{
name|envEdge
operator|.
name|incValue
argument_list|(
literal|70000
argument_list|)
expr_stmt|;
block|}
specifier|static
name|void
name|clearQuotaCache
parameter_list|(
name|HBaseTestingUtility
name|testUtil
parameter_list|)
block|{
for|for
control|(
name|RegionServerThread
name|rst
range|:
name|testUtil
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegionServerThreads
argument_list|()
control|)
block|{
name|RegionServerRpcQuotaManager
name|quotaManager
init|=
name|rst
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getRegionServerRpcQuotaManager
argument_list|()
decl_stmt|;
name|QuotaCache
name|quotaCache
init|=
name|quotaManager
operator|.
name|getQuotaCache
argument_list|()
decl_stmt|;
name|quotaCache
operator|.
name|getNamespaceQuotaCache
argument_list|()
operator|.
name|clear
argument_list|()
expr_stmt|;
name|quotaCache
operator|.
name|getTableQuotaCache
argument_list|()
operator|.
name|clear
argument_list|()
expr_stmt|;
name|quotaCache
operator|.
name|getUserQuotaCache
argument_list|()
operator|.
name|clear
argument_list|()
expr_stmt|;
name|quotaCache
operator|.
name|getRegionServerQuotaCache
argument_list|()
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit
