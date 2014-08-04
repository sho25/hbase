begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
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
name|commons
operator|.
name|math
operator|.
name|stat
operator|.
name|descriptive
operator|.
name|DescriptiveStatistics
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
name|chaos
operator|.
name|actions
operator|.
name|MoveRandomRegionOfTableAction
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
name|chaos
operator|.
name|actions
operator|.
name|RestartRsHoldingTableAction
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
name|chaos
operator|.
name|monkies
operator|.
name|PolicyBasedChaosMonkey
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
name|chaos
operator|.
name|policies
operator|.
name|PeriodicRandomActionPolicy
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
name|chaos
operator|.
name|policies
operator|.
name|Policy
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
name|ipc
operator|.
name|RpcClient
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
name|regionserver
operator|.
name|DisabledRegionSplitPolicy
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
name|mapreduce
operator|.
name|Counters
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
name|mapreduce
operator|.
name|Job
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
name|util
operator|.
name|ToolRunner
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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Queue
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Callable
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|lang
operator|.
name|String
operator|.
name|format
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
name|assertTrue
import|;
end_import

begin_comment
comment|/**  * Test for comparing the performance impact of region replicas. Uses  * components of {@link PerformanceEvaluation}. Does not run from  * {@code IntegrationTestsDriver} because IntegrationTestBase is incompatible  * with the JUnit runner. Hence no @Test annotations either. See {@code -help}  * for full list of options.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|IntegrationTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|IntegrationTestRegionReplicaPerf
extends|extends
name|IntegrationTestBase
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
name|IntegrationTestRegionReplicaPerf
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|SLEEP_TIME_KEY
init|=
literal|"sleeptime"
decl_stmt|;
comment|// short default interval because tests don't run very long.
specifier|private
specifier|static
specifier|final
name|String
name|SLEEP_TIME_DEFAULT
init|=
literal|""
operator|+
operator|(
literal|10
operator|*
literal|1000l
operator|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TABLE_NAME_KEY
init|=
literal|"tableName"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TABLE_NAME_DEFAULT
init|=
literal|"IntegrationTestRegionReplicaPerf"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|NOMAPRED_KEY
init|=
literal|"nomapred"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|boolean
name|NOMAPRED_DEFAULT
init|=
literal|false
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|REPLICA_COUNT_KEY
init|=
literal|"replicas"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|REPLICA_COUNT_DEFAULT
init|=
literal|""
operator|+
literal|3
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|PRIMARY_TIMEOUT_KEY
init|=
literal|"timeout"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|PRIMARY_TIMEOUT_DEFAULT
init|=
literal|""
operator|+
literal|10
operator|*
literal|1000
decl_stmt|;
comment|// 10 ms
specifier|private
specifier|static
specifier|final
name|String
name|NUM_RS_KEY
init|=
literal|"numRs"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|NUM_RS_DEFAULT
init|=
literal|""
operator|+
literal|3
decl_stmt|;
specifier|private
name|TableName
name|tableName
decl_stmt|;
specifier|private
name|long
name|sleepTime
decl_stmt|;
specifier|private
name|boolean
name|nomapred
init|=
name|NOMAPRED_DEFAULT
decl_stmt|;
specifier|private
name|int
name|replicaCount
decl_stmt|;
specifier|private
name|int
name|primaryTimeout
decl_stmt|;
specifier|private
name|int
name|clusterSize
decl_stmt|;
comment|/**    * Wraps the invocation of {@link PerformanceEvaluation} in a {@code Callable}.    */
specifier|static
class|class
name|PerfEvalCallable
implements|implements
name|Callable
argument_list|<
name|TimingResult
argument_list|>
block|{
specifier|private
specifier|final
name|Queue
argument_list|<
name|String
argument_list|>
name|argv
init|=
operator|new
name|LinkedList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Admin
name|admin
decl_stmt|;
specifier|public
name|PerfEvalCallable
parameter_list|(
name|Admin
name|admin
parameter_list|,
name|String
name|argv
parameter_list|)
block|{
comment|// TODO: this API is awkward, should take HConnection, not HBaseAdmin
name|this
operator|.
name|admin
operator|=
name|admin
expr_stmt|;
name|this
operator|.
name|argv
operator|.
name|addAll
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|argv
operator|.
name|split
argument_list|(
literal|" "
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Created PerformanceEvaluationCallable with args: "
operator|+
name|argv
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|TimingResult
name|call
parameter_list|()
throws|throws
name|Exception
block|{
name|PerformanceEvaluation
operator|.
name|TestOptions
name|opts
init|=
name|PerformanceEvaluation
operator|.
name|parseOpts
argument_list|(
name|argv
argument_list|)
decl_stmt|;
name|PerformanceEvaluation
operator|.
name|checkTable
argument_list|(
name|admin
argument_list|,
name|opts
argument_list|)
expr_stmt|;
name|long
name|numRows
init|=
name|opts
operator|.
name|totalRows
decl_stmt|;
name|long
name|elapsedTime
decl_stmt|;
if|if
condition|(
name|opts
operator|.
name|nomapred
condition|)
block|{
name|elapsedTime
operator|=
name|PerformanceEvaluation
operator|.
name|doLocalClients
argument_list|(
name|opts
argument_list|,
name|admin
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Job
name|job
init|=
name|PerformanceEvaluation
operator|.
name|doMapReduce
argument_list|(
name|opts
argument_list|,
name|admin
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|Counters
name|counters
init|=
name|job
operator|.
name|getCounters
argument_list|()
decl_stmt|;
name|numRows
operator|=
name|counters
operator|.
name|findCounter
argument_list|(
name|PerformanceEvaluation
operator|.
name|Counter
operator|.
name|ROWS
argument_list|)
operator|.
name|getValue
argument_list|()
expr_stmt|;
name|elapsedTime
operator|=
name|counters
operator|.
name|findCounter
argument_list|(
name|PerformanceEvaluation
operator|.
name|Counter
operator|.
name|ELAPSED_TIME
argument_list|)
operator|.
name|getValue
argument_list|()
expr_stmt|;
block|}
return|return
operator|new
name|TimingResult
argument_list|(
name|numRows
argument_list|,
name|elapsedTime
argument_list|)
return|;
block|}
block|}
comment|/**    * Record the results from a single {@link PerformanceEvaluation} job run.    */
specifier|static
class|class
name|TimingResult
block|{
specifier|public
name|long
name|numRows
decl_stmt|;
specifier|public
name|long
name|elapsedTime
decl_stmt|;
specifier|public
name|TimingResult
parameter_list|(
name|long
name|numRows
parameter_list|,
name|long
name|elapsedTime
parameter_list|)
block|{
name|this
operator|.
name|numRows
operator|=
name|numRows
expr_stmt|;
name|this
operator|.
name|elapsedTime
operator|=
name|elapsedTime
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|Objects
operator|.
name|toStringHelper
argument_list|(
name|this
argument_list|)
operator|.
name|add
argument_list|(
literal|"numRows"
argument_list|,
name|numRows
argument_list|)
operator|.
name|add
argument_list|(
literal|"elapsedTime"
argument_list|,
name|elapsedTime
argument_list|)
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
name|Configuration
name|conf
init|=
name|util
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
comment|// sanity check cluster
comment|// TODO: this should reach out to master and verify online state instead
name|assertEquals
argument_list|(
literal|"Master must be configured with StochasticLoadBalancer"
argument_list|,
literal|"org.apache.hadoop.hbase.master.balancer.StochasticLoadBalancer"
argument_list|,
name|conf
operator|.
name|get
argument_list|(
literal|"hbase.master.loadbalancer.class"
argument_list|)
argument_list|)
expr_stmt|;
comment|// TODO: this should reach out to master and verify online state instead
name|assertTrue
argument_list|(
literal|"hbase.regionserver.storefile.refresh.period must be greater than zero."
argument_list|,
name|conf
operator|.
name|getLong
argument_list|(
literal|"hbase.regionserver.storefile.refresh.period"
argument_list|,
literal|0
argument_list|)
operator|>
literal|0
argument_list|)
expr_stmt|;
comment|// enable client-side settings
name|conf
operator|.
name|setBoolean
argument_list|(
name|RpcClient
operator|.
name|SPECIFIC_WRITE_THREAD
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// TODO: expose these settings to CLI override
name|conf
operator|.
name|setLong
argument_list|(
literal|"hbase.client.primaryCallTimeout.get"
argument_list|,
name|primaryTimeout
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
literal|"hbase.client.primaryCallTimeout.multiget"
argument_list|,
name|primaryTimeout
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setUpCluster
parameter_list|()
throws|throws
name|Exception
block|{
name|util
operator|=
name|getTestingUtil
argument_list|(
name|getConf
argument_list|()
argument_list|)
expr_stmt|;
name|util
operator|.
name|initializeCluster
argument_list|(
name|clusterSize
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setUpMonkey
parameter_list|()
throws|throws
name|Exception
block|{
name|Policy
name|p
init|=
operator|new
name|PeriodicRandomActionPolicy
argument_list|(
name|sleepTime
argument_list|,
operator|new
name|RestartRsHoldingTableAction
argument_list|(
name|sleepTime
argument_list|,
name|tableName
operator|.
name|getNameAsString
argument_list|()
argument_list|)
argument_list|,
operator|new
name|MoveRandomRegionOfTableAction
argument_list|(
name|tableName
operator|.
name|getNameAsString
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|this
operator|.
name|monkey
operator|=
operator|new
name|PolicyBasedChaosMonkey
argument_list|(
name|util
argument_list|,
name|p
argument_list|)
expr_stmt|;
comment|// don't start monkey right away
block|}
annotation|@
name|Override
specifier|protected
name|void
name|addOptions
parameter_list|()
block|{
name|addOptWithArg
argument_list|(
name|TABLE_NAME_KEY
argument_list|,
literal|"Alternate table name. Default: '"
operator|+
name|TABLE_NAME_DEFAULT
operator|+
literal|"'"
argument_list|)
expr_stmt|;
name|addOptWithArg
argument_list|(
name|SLEEP_TIME_KEY
argument_list|,
literal|"How long the monkey sleeps between actions. Default: "
operator|+
name|SLEEP_TIME_DEFAULT
argument_list|)
expr_stmt|;
name|addOptNoArg
argument_list|(
name|NOMAPRED_KEY
argument_list|,
literal|"Run multiple clients using threads (rather than use mapreduce)"
argument_list|)
expr_stmt|;
name|addOptWithArg
argument_list|(
name|REPLICA_COUNT_KEY
argument_list|,
literal|"Number of region replicas. Default: "
operator|+
name|REPLICA_COUNT_DEFAULT
argument_list|)
expr_stmt|;
name|addOptWithArg
argument_list|(
name|PRIMARY_TIMEOUT_KEY
argument_list|,
literal|"Overrides hbase.client.primaryCallTimeout. Default: "
operator|+
name|PRIMARY_TIMEOUT_DEFAULT
operator|+
literal|" (10ms)"
argument_list|)
expr_stmt|;
name|addOptWithArg
argument_list|(
name|NUM_RS_KEY
argument_list|,
literal|"Specify the number of RegionServers to use. Default: "
operator|+
name|NUM_RS_DEFAULT
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
name|tableName
operator|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|TABLE_NAME_KEY
argument_list|,
name|TABLE_NAME_DEFAULT
argument_list|)
argument_list|)
expr_stmt|;
name|sleepTime
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|SLEEP_TIME_KEY
argument_list|,
name|SLEEP_TIME_DEFAULT
argument_list|)
argument_list|)
expr_stmt|;
name|nomapred
operator|=
name|cmd
operator|.
name|hasOption
argument_list|(
name|NOMAPRED_KEY
argument_list|)
expr_stmt|;
name|replicaCount
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|REPLICA_COUNT_KEY
argument_list|,
name|REPLICA_COUNT_DEFAULT
argument_list|)
argument_list|)
expr_stmt|;
name|primaryTimeout
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|PRIMARY_TIMEOUT_KEY
argument_list|,
name|PRIMARY_TIMEOUT_DEFAULT
argument_list|)
argument_list|)
expr_stmt|;
name|clusterSize
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|NUM_RS_KEY
argument_list|,
name|NUM_RS_DEFAULT
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|Objects
operator|.
name|toStringHelper
argument_list|(
literal|"Parsed Options"
argument_list|)
operator|.
name|add
argument_list|(
name|TABLE_NAME_KEY
argument_list|,
name|tableName
argument_list|)
operator|.
name|add
argument_list|(
name|SLEEP_TIME_KEY
argument_list|,
name|sleepTime
argument_list|)
operator|.
name|add
argument_list|(
name|NOMAPRED_KEY
argument_list|,
name|nomapred
argument_list|)
operator|.
name|add
argument_list|(
name|REPLICA_COUNT_KEY
argument_list|,
name|replicaCount
argument_list|)
operator|.
name|add
argument_list|(
name|PRIMARY_TIMEOUT_KEY
argument_list|,
name|primaryTimeout
argument_list|)
operator|.
name|add
argument_list|(
name|NUM_RS_KEY
argument_list|,
name|clusterSize
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|runTestFromCommandLine
parameter_list|()
throws|throws
name|Exception
block|{
name|test
argument_list|()
expr_stmt|;
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getTablename
parameter_list|()
block|{
return|return
name|tableName
operator|.
name|getNameAsString
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|protected
name|Set
argument_list|<
name|String
argument_list|>
name|getColumnFamilies
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
specifier|public
name|void
name|test
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|maxIters
init|=
literal|3
decl_stmt|;
name|String
name|mr
init|=
name|nomapred
condition|?
literal|"--nomapred"
else|:
literal|""
decl_stmt|;
name|String
name|replicas
init|=
literal|"--replicas="
operator|+
name|replicaCount
decl_stmt|;
comment|// TODO: splits disabled until "phase 2" is complete.
name|String
name|splitPolicy
init|=
literal|"--splitPolicy="
operator|+
name|DisabledRegionSplitPolicy
operator|.
name|class
operator|.
name|getName
argument_list|()
decl_stmt|;
name|String
name|writeOpts
init|=
name|format
argument_list|(
literal|"%s %s --table=%s --presplit=16 sequentialWrite 4"
argument_list|,
name|mr
argument_list|,
name|splitPolicy
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|String
name|readOpts
init|=
name|format
argument_list|(
literal|"%s --table=%s --latency --sampleRate=0.1 randomRead 4"
argument_list|,
name|mr
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|String
name|replicaReadOpts
init|=
name|format
argument_list|(
literal|"%s %s"
argument_list|,
name|replicas
argument_list|,
name|readOpts
argument_list|)
decl_stmt|;
name|ArrayList
argument_list|<
name|TimingResult
argument_list|>
name|resultsWithoutReplica
init|=
operator|new
name|ArrayList
argument_list|<
name|TimingResult
argument_list|>
argument_list|(
name|maxIters
argument_list|)
decl_stmt|;
name|ArrayList
argument_list|<
name|TimingResult
argument_list|>
name|resultsWithReplica
init|=
operator|new
name|ArrayList
argument_list|<
name|TimingResult
argument_list|>
argument_list|(
name|maxIters
argument_list|)
decl_stmt|;
comment|// create/populate the table, replicas disabled
name|LOG
operator|.
name|debug
argument_list|(
literal|"Populating table."
argument_list|)
expr_stmt|;
operator|new
name|PerfEvalCallable
argument_list|(
name|util
operator|.
name|getHBaseAdmin
argument_list|()
argument_list|,
name|writeOpts
argument_list|)
operator|.
name|call
argument_list|()
expr_stmt|;
comment|// one last sanity check, then send in the clowns!
name|assertEquals
argument_list|(
literal|"Table must be created with DisabledRegionSplitPolicy. Broken test."
argument_list|,
name|DisabledRegionSplitPolicy
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|,
name|util
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|getTableDescriptor
argument_list|(
name|tableName
argument_list|)
operator|.
name|getRegionSplitPolicyClassName
argument_list|()
argument_list|)
expr_stmt|;
name|startMonkey
argument_list|()
expr_stmt|;
comment|// collect a baseline without region replicas.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|maxIters
condition|;
name|i
operator|++
control|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Launching non-replica job "
operator|+
operator|(
name|i
operator|+
literal|1
operator|)
operator|+
literal|"/"
operator|+
name|maxIters
argument_list|)
expr_stmt|;
name|resultsWithoutReplica
operator|.
name|add
argument_list|(
operator|new
name|PerfEvalCallable
argument_list|(
name|util
operator|.
name|getHBaseAdmin
argument_list|()
argument_list|,
name|readOpts
argument_list|)
operator|.
name|call
argument_list|()
argument_list|)
expr_stmt|;
comment|// TODO: sleep to let cluster stabilize, though monkey continues. is it necessary?
name|Thread
operator|.
name|sleep
argument_list|(
literal|5000l
argument_list|)
expr_stmt|;
block|}
comment|// disable monkey, enable region replicas, enable monkey
name|cleanUpMonkey
argument_list|(
literal|"Altering table."
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Altering "
operator|+
name|tableName
operator|+
literal|" replica count to "
operator|+
name|replicaCount
argument_list|)
expr_stmt|;
name|util
operator|.
name|setReplicas
argument_list|(
name|util
operator|.
name|getHBaseAdmin
argument_list|()
argument_list|,
name|tableName
argument_list|,
name|replicaCount
argument_list|)
expr_stmt|;
name|setUpMonkey
argument_list|()
expr_stmt|;
name|startMonkey
argument_list|()
expr_stmt|;
comment|// run test with region replicas.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|maxIters
condition|;
name|i
operator|++
control|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Launching replica job "
operator|+
operator|(
name|i
operator|+
literal|1
operator|)
operator|+
literal|"/"
operator|+
name|maxIters
argument_list|)
expr_stmt|;
name|resultsWithReplica
operator|.
name|add
argument_list|(
operator|new
name|PerfEvalCallable
argument_list|(
name|util
operator|.
name|getHBaseAdmin
argument_list|()
argument_list|,
name|replicaReadOpts
argument_list|)
operator|.
name|call
argument_list|()
argument_list|)
expr_stmt|;
comment|// TODO: sleep to let cluster stabilize, though monkey continues. is it necessary?
name|Thread
operator|.
name|sleep
argument_list|(
literal|5000l
argument_list|)
expr_stmt|;
block|}
name|DescriptiveStatistics
name|withoutReplicaStats
init|=
operator|new
name|DescriptiveStatistics
argument_list|()
decl_stmt|;
for|for
control|(
name|TimingResult
name|tr
range|:
name|resultsWithoutReplica
control|)
block|{
name|withoutReplicaStats
operator|.
name|addValue
argument_list|(
name|tr
operator|.
name|elapsedTime
argument_list|)
expr_stmt|;
block|}
name|DescriptiveStatistics
name|withReplicaStats
init|=
operator|new
name|DescriptiveStatistics
argument_list|()
decl_stmt|;
for|for
control|(
name|TimingResult
name|tr
range|:
name|resultsWithReplica
control|)
block|{
name|withReplicaStats
operator|.
name|addValue
argument_list|(
name|tr
operator|.
name|elapsedTime
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
name|Objects
operator|.
name|toStringHelper
argument_list|(
literal|"testName"
argument_list|)
operator|.
name|add
argument_list|(
literal|"withoutReplicas"
argument_list|,
name|resultsWithoutReplica
argument_list|)
operator|.
name|add
argument_list|(
literal|"withReplicas"
argument_list|,
name|resultsWithReplica
argument_list|)
operator|.
name|add
argument_list|(
literal|"withoutReplicasMean"
argument_list|,
name|withoutReplicaStats
operator|.
name|getMean
argument_list|()
argument_list|)
operator|.
name|add
argument_list|(
literal|"withReplicasMean"
argument_list|,
name|withReplicaStats
operator|.
name|getMean
argument_list|()
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Running with region replicas under chaos should be as fast or faster than without. "
operator|+
literal|"withReplicas.mean: "
operator|+
name|withReplicaStats
operator|.
name|getMean
argument_list|()
operator|+
literal|"ms "
operator|+
literal|"withoutReplicas.mean: "
operator|+
name|withoutReplicaStats
operator|.
name|getMean
argument_list|()
operator|+
literal|"ms."
argument_list|,
name|withReplicaStats
operator|.
name|getMean
argument_list|()
operator|<=
name|withoutReplicaStats
operator|.
name|getMean
argument_list|()
argument_list|)
expr_stmt|;
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
name|Exception
block|{
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|IntegrationTestingUtility
operator|.
name|setUseDistributedCluster
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|int
name|status
init|=
name|ToolRunner
operator|.
name|run
argument_list|(
name|conf
argument_list|,
operator|new
name|IntegrationTestRegionReplicaPerf
argument_list|()
argument_list|,
name|args
argument_list|)
decl_stmt|;
name|System
operator|.
name|exit
argument_list|(
name|status
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

