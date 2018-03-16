begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
operator|.
name|balancer
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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
name|hadoop
operator|.
name|hbase
operator|.
name|HBaseCommonTestingUtility
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
name|ServerName
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
name|RegionInfo
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
name|RegionInfoBuilder
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
name|LoadBalancer
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
name|AbstractHBaseTool
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Stopwatch
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|CommandLine
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|Option
import|;
end_import

begin_comment
comment|/**  * Tool to test performance of different {@link org.apache.hadoop.hbase.master.LoadBalancer}  * implementations.  * Example command:  * $ bin/hbase org.apache.hadoop.hbase.master.balancer.LoadBalancerPerformanceEvaluation  *   -regions 1000 -servers 100  *   -load_balancer org.apache.hadoop.hbase.master.balancer.SimpleLoadBalancer  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|TOOLS
argument_list|)
specifier|public
class|class
name|LoadBalancerPerformanceEvaluation
extends|extends
name|AbstractHBaseTool
block|{
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
name|LoadBalancerPerformanceEvaluation
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|HBaseCommonTestingUtility
name|UTIL
init|=
operator|new
name|HBaseCommonTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|DEFAULT_NUM_REGIONS
init|=
literal|1000000
decl_stmt|;
specifier|private
specifier|static
name|Option
name|NUM_REGIONS_OPT
init|=
operator|new
name|Option
argument_list|(
literal|"regions"
argument_list|,
literal|true
argument_list|,
literal|"Number of regions to consider by load balancer. Default: "
operator|+
name|DEFAULT_NUM_REGIONS
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|DEFAULT_NUM_SERVERS
init|=
literal|1000
decl_stmt|;
specifier|private
specifier|static
name|Option
name|NUM_SERVERS_OPT
init|=
operator|new
name|Option
argument_list|(
literal|"servers"
argument_list|,
literal|true
argument_list|,
literal|"Number of servers to consider by load balancer. Default: "
operator|+
name|DEFAULT_NUM_SERVERS
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|DEFAULT_LOAD_BALANCER
init|=
literal|"org.apache.hadoop.hbase.master.balancer.StochasticLoadBalancer"
decl_stmt|;
specifier|private
specifier|static
name|Option
name|LOAD_BALANCER_OPT
init|=
operator|new
name|Option
argument_list|(
literal|"load_balancer"
argument_list|,
literal|true
argument_list|,
literal|"Type of Load Balancer to use. Default: "
operator|+
name|DEFAULT_LOAD_BALANCER
argument_list|)
decl_stmt|;
specifier|private
name|int
name|numRegions
decl_stmt|;
specifier|private
name|int
name|numServers
decl_stmt|;
specifier|private
name|String
name|loadBalancerType
decl_stmt|;
specifier|private
name|Class
argument_list|<
name|?
argument_list|>
name|loadBalancerClazz
decl_stmt|;
specifier|private
name|LoadBalancer
name|loadBalancer
decl_stmt|;
comment|// data
specifier|private
name|List
argument_list|<
name|ServerName
argument_list|>
name|servers
decl_stmt|;
specifier|private
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|regions
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|RegionInfo
argument_list|,
name|ServerName
argument_list|>
name|regionServerMap
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|RegionInfo
argument_list|>
argument_list|>
name|serverRegionMap
decl_stmt|;
comment|// Non-default configurations.
specifier|private
name|void
name|setupConf
parameter_list|()
block|{
name|conf
operator|.
name|setClass
argument_list|(
name|HConstants
operator|.
name|HBASE_MASTER_LOADBALANCER_CLASS
argument_list|,
name|loadBalancerClazz
argument_list|,
name|LoadBalancer
operator|.
name|class
argument_list|)
expr_stmt|;
name|loadBalancer
operator|=
name|LoadBalancerFactory
operator|.
name|getLoadBalancer
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|generateRegionsAndServers
parameter_list|()
block|{
comment|// regions
name|regions
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|numRegions
argument_list|)
expr_stmt|;
name|regionServerMap
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|numRegions
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
name|numRegions
condition|;
operator|++
name|i
control|)
block|{
name|byte
index|[]
name|start
init|=
operator|new
name|byte
index|[
literal|16
index|]
decl_stmt|;
name|byte
index|[]
name|end
init|=
operator|new
name|byte
index|[
literal|16
index|]
decl_stmt|;
name|Bytes
operator|.
name|putInt
argument_list|(
name|start
argument_list|,
literal|0
argument_list|,
name|i
argument_list|)
expr_stmt|;
name|Bytes
operator|.
name|putInt
argument_list|(
name|end
argument_list|,
literal|0
argument_list|,
name|i
operator|+
literal|1
argument_list|)
expr_stmt|;
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"LoadBalancerPerfTable"
argument_list|)
decl_stmt|;
name|RegionInfo
name|hri
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|tableName
argument_list|)
operator|.
name|setStartKey
argument_list|(
name|start
argument_list|)
operator|.
name|setEndKey
argument_list|(
name|end
argument_list|)
operator|.
name|setSplit
argument_list|(
literal|false
argument_list|)
operator|.
name|setRegionId
argument_list|(
name|i
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|regions
operator|.
name|add
argument_list|(
name|hri
argument_list|)
expr_stmt|;
name|regionServerMap
operator|.
name|put
argument_list|(
name|hri
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|// servers
name|servers
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|numServers
argument_list|)
expr_stmt|;
name|serverRegionMap
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|numServers
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
name|numServers
condition|;
operator|++
name|i
control|)
block|{
name|ServerName
name|sn
init|=
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"srv"
operator|+
name|i
argument_list|,
name|HConstants
operator|.
name|DEFAULT_REGIONSERVER_PORT
argument_list|,
name|i
argument_list|)
decl_stmt|;
name|servers
operator|.
name|add
argument_list|(
name|sn
argument_list|)
expr_stmt|;
name|serverRegionMap
operator|.
name|put
argument_list|(
name|sn
argument_list|,
name|i
operator|==
literal|0
condition|?
name|regions
else|:
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|addOptions
parameter_list|()
block|{
name|addOption
argument_list|(
name|NUM_REGIONS_OPT
argument_list|)
expr_stmt|;
name|addOption
argument_list|(
name|NUM_SERVERS_OPT
argument_list|)
expr_stmt|;
name|addOption
argument_list|(
name|LOAD_BALANCER_OPT
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|processOptions
parameter_list|(
name|CommandLine
name|cmd
parameter_list|)
block|{
name|numRegions
operator|=
name|getOptionAsInt
argument_list|(
name|cmd
argument_list|,
name|NUM_REGIONS_OPT
operator|.
name|getOpt
argument_list|()
argument_list|,
name|DEFAULT_NUM_REGIONS
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|numRegions
operator|>
literal|0
argument_list|,
literal|"Invalid number of regions!"
argument_list|)
expr_stmt|;
name|numServers
operator|=
name|getOptionAsInt
argument_list|(
name|cmd
argument_list|,
name|NUM_SERVERS_OPT
operator|.
name|getOpt
argument_list|()
argument_list|,
name|DEFAULT_NUM_SERVERS
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|numServers
operator|>
literal|0
argument_list|,
literal|"Invalid number of servers!"
argument_list|)
expr_stmt|;
name|loadBalancerType
operator|=
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|LOAD_BALANCER_OPT
operator|.
name|getOpt
argument_list|()
argument_list|,
name|DEFAULT_LOAD_BALANCER
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkArgument
argument_list|(
operator|!
name|loadBalancerType
operator|.
name|isEmpty
argument_list|()
argument_list|,
literal|"Invalid load balancer type!"
argument_list|)
expr_stmt|;
try|try
block|{
name|loadBalancerClazz
operator|=
name|Class
operator|.
name|forName
argument_list|(
name|loadBalancerType
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Class '"
operator|+
name|loadBalancerType
operator|+
literal|"' not found!"
argument_list|)
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
name|setupConf
argument_list|()
expr_stmt|;
block|}
specifier|private
name|String
name|formatResults
parameter_list|(
specifier|final
name|String
name|methodName
parameter_list|,
specifier|final
name|long
name|timeMillis
parameter_list|)
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"Time for %-25s: %dms%n"
argument_list|,
name|methodName
argument_list|,
name|timeMillis
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|int
name|doWork
parameter_list|()
throws|throws
name|Exception
block|{
name|generateRegionsAndServers
argument_list|()
expr_stmt|;
name|String
name|methodName
init|=
literal|"roundRobinAssignment"
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Calling "
operator|+
name|methodName
argument_list|)
expr_stmt|;
name|Stopwatch
name|watch
init|=
name|Stopwatch
operator|.
name|createStarted
argument_list|()
decl_stmt|;
name|loadBalancer
operator|.
name|roundRobinAssignment
argument_list|(
name|regions
argument_list|,
name|servers
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|print
argument_list|(
name|formatResults
argument_list|(
name|methodName
argument_list|,
name|watch
operator|.
name|elapsed
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|methodName
operator|=
literal|"retainAssignment"
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Calling "
operator|+
name|methodName
argument_list|)
expr_stmt|;
name|watch
operator|.
name|reset
argument_list|()
operator|.
name|start
argument_list|()
expr_stmt|;
name|loadBalancer
operator|.
name|retainAssignment
argument_list|(
name|regionServerMap
argument_list|,
name|servers
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|print
argument_list|(
name|formatResults
argument_list|(
name|methodName
argument_list|,
name|watch
operator|.
name|elapsed
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|methodName
operator|=
literal|"balanceCluster"
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Calling "
operator|+
name|methodName
argument_list|)
expr_stmt|;
name|watch
operator|.
name|reset
argument_list|()
operator|.
name|start
argument_list|()
expr_stmt|;
name|loadBalancer
operator|.
name|balanceCluster
argument_list|(
name|serverRegionMap
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|print
argument_list|(
name|formatResults
argument_list|(
name|methodName
argument_list|,
name|watch
operator|.
name|elapsed
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|EXIT_SUCCESS
return|;
block|}
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|IOException
block|{
name|LoadBalancerPerformanceEvaluation
name|tool
init|=
operator|new
name|LoadBalancerPerformanceEvaluation
argument_list|()
decl_stmt|;
name|tool
operator|.
name|setConf
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|tool
operator|.
name|run
argument_list|(
name|args
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

