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
name|CategoryBasedTimeout
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
name|MiniHBaseCluster
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
name|master
operator|.
name|HMaster
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
name|regionserver
operator|.
name|HRegion
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
name|JVMClusterUtil
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
name|org
operator|.
name|junit
operator|.
name|After
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Ignore
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

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|rules
operator|.
name|TestRule
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
name|java
operator|.
name|util
operator|.
name|List
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
comment|/**  * Test options for regions on master; none, system, or any (i.e. master is like any other  * regionserver). Checks how regions are deployed when each of the options are enabled.  * It then does kill combinations to make sure the distribution is more than just for startup.  * NOTE: Regions on Master does not work well. See HBASE-19828. Until addressed, disabling this  * test.  */
end_comment

begin_class
annotation|@
name|Ignore
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
name|TestRegionsOnMasterOptions
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
name|TestRegionsOnMasterOptions
operator|.
name|class
argument_list|)
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
name|Rule
specifier|public
specifier|final
name|TestRule
name|timeout
init|=
name|CategoryBasedTimeout
operator|.
name|builder
argument_list|()
operator|.
name|withTimeout
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
operator|.
name|withLookingForStuckThread
argument_list|(
literal|true
argument_list|)
operator|.
name|build
argument_list|()
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
name|Configuration
name|c
decl_stmt|;
specifier|private
name|String
name|tablesOnMasterOldValue
decl_stmt|;
specifier|private
name|String
name|systemTablesOnMasterOldValue
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|SLAVES
init|=
literal|3
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|MASTERS
init|=
literal|2
decl_stmt|;
comment|// Make the count of REGIONS high enough so I can distingush case where master is only carrying
comment|// system regions from the case where it is carrying any region; i.e. 2 system regions vs more
comment|// if user + system.
specifier|private
specifier|static
specifier|final
name|int
name|REGIONS
init|=
literal|12
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|SYSTEM_REGIONS
init|=
literal|2
decl_stmt|;
comment|// ns and meta -- no acl unless enabled.
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
block|{
name|this
operator|.
name|c
operator|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|this
operator|.
name|tablesOnMasterOldValue
operator|=
name|c
operator|.
name|get
argument_list|(
name|LoadBalancer
operator|.
name|TABLES_ON_MASTER
argument_list|)
expr_stmt|;
name|this
operator|.
name|systemTablesOnMasterOldValue
operator|=
name|c
operator|.
name|get
argument_list|(
name|LoadBalancer
operator|.
name|SYSTEM_TABLES_ON_MASTER
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
block|{
name|unset
argument_list|(
name|LoadBalancer
operator|.
name|TABLES_ON_MASTER
argument_list|,
name|this
operator|.
name|tablesOnMasterOldValue
argument_list|)
expr_stmt|;
name|unset
argument_list|(
name|LoadBalancer
operator|.
name|SYSTEM_TABLES_ON_MASTER
argument_list|,
name|this
operator|.
name|systemTablesOnMasterOldValue
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|unset
parameter_list|(
specifier|final
name|String
name|key
parameter_list|,
specifier|final
name|String
name|value
parameter_list|)
block|{
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|c
operator|.
name|unset
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|c
operator|.
name|set
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRegionsOnAllServers
parameter_list|()
throws|throws
name|Exception
block|{
name|c
operator|.
name|setBoolean
argument_list|(
name|LoadBalancer
operator|.
name|TABLES_ON_MASTER
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|c
operator|.
name|setBoolean
argument_list|(
name|LoadBalancer
operator|.
name|SYSTEM_TABLES_ON_MASTER
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|int
name|rsCount
init|=
operator|(
name|REGIONS
operator|+
name|SYSTEM_REGIONS
operator|)
operator|/
operator|(
name|SLAVES
operator|+
literal|1
comment|/*Master*/
operator|)
decl_stmt|;
name|checkBalance
argument_list|(
name|rsCount
argument_list|,
name|rsCount
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNoRegionOnMaster
parameter_list|()
throws|throws
name|Exception
block|{
name|c
operator|.
name|setBoolean
argument_list|(
name|LoadBalancer
operator|.
name|TABLES_ON_MASTER
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|c
operator|.
name|setBoolean
argument_list|(
name|LoadBalancer
operator|.
name|SYSTEM_TABLES_ON_MASTER
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|int
name|rsCount
init|=
operator|(
name|REGIONS
operator|+
name|SYSTEM_REGIONS
operator|)
operator|/
name|SLAVES
decl_stmt|;
name|checkBalance
argument_list|(
literal|0
argument_list|,
name|rsCount
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Ignore
comment|// Fix this. The Master startup doesn't allow Master reporting as a RegionServer, not
comment|// until way late after the Master startup finishes. Needs more work.
annotation|@
name|Test
specifier|public
name|void
name|testSystemTablesOnMaster
parameter_list|()
throws|throws
name|Exception
block|{
name|c
operator|.
name|setBoolean
argument_list|(
name|LoadBalancer
operator|.
name|TABLES_ON_MASTER
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|c
operator|.
name|setBoolean
argument_list|(
name|LoadBalancer
operator|.
name|SYSTEM_TABLES_ON_MASTER
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// IS THIS SHORT-CIRCUIT RPC? Yes. Here is how it looks currently if I have an exception
comment|// thrown in doBatchMutate inside a Region.
comment|//
comment|//    java.lang.Exception
comment|//    at org.apache.hadoop.hbase.regionserver.HRegion.doBatchMutate(HRegion.java:3845)
comment|//    at org.apache.hadoop.hbase.regionserver.HRegion.put(HRegion.java:2972)
comment|//    at org.apache.hadoop.hbase.regionserver.RSRpcServices.mutate(RSRpcServices.java:2751)
comment|//    at org.apache.hadoop.hbase.client.ClientServiceCallable.doMutate(ClientServiceCallable.java:55)
comment|//    at org.apache.hadoop.hbase.client.HTable$3.rpcCall(HTable.java:585)
comment|//    at org.apache.hadoop.hbase.client.HTable$3.rpcCall(HTable.java:579)
comment|//    at org.apache.hadoop.hbase.client.RegionServerCallable.call(RegionServerCallable.java:126)
comment|//    at org.apache.hadoop.hbase.client.RpcRetryingCallerImpl.callWithRetries(RpcRetryingCallerImpl.java:106)
comment|//    at org.apache.hadoop.hbase.client.HTable.put(HTable.java:589)
comment|//    at org.apache.hadoop.hbase.master.TableNamespaceManager.insertIntoNSTable(TableNamespaceManager.java:156)
comment|//    at org.apache.hadoop.hbase.master.procedure.CreateNamespaceProcedure.insertIntoNSTable(CreateNamespaceProcedure.java:222)
comment|//    at org.apache.hadoop.hbase.master.procedure.CreateNamespaceProcedure.executeFromState(CreateNamespaceProcedure.java:76)
comment|//    at org.apache.hadoop.hbase.master.procedure.CreateNamespaceProcedure.executeFromState(CreateNamespaceProcedure.java:40)
comment|//    at org.apache.hadoop.hbase.procedure2.StateMachineProcedure.execute(StateMachineProcedure.java:181)
comment|//    at org.apache.hadoop.hbase.procedure2.Procedure.doExecute(Procedure.java:847)
comment|//    at org.apache.hadoop.hbase.procedure2.ProcedureExecutor.execProcedure(ProcedureExecutor.java:1440)
comment|//    at org.apache.hadoop.hbase.procedure2.ProcedureExecutor.executeProcedure(ProcedureExecutor.java:1209)
comment|//    at org.apache.hadoop.hbase.procedure2.ProcedureExecutor.access$800(ProcedureExecutor.java:79)
comment|//    at org.apache.hadoop.hbase.procedure2.ProcedureExecutor$WorkerThread.run(ProcedureExecutor.java:1719)
comment|//
comment|// If I comment out the ConnectionUtils ConnectionImplementation content, I see this:
comment|//
comment|//    java.lang.Exception
comment|//    at org.apache.hadoop.hbase.regionserver.HRegion.doBatchMutate(HRegion.java:3845)
comment|//    at org.apache.hadoop.hbase.regionserver.HRegion.put(HRegion.java:2972)
comment|//    at org.apache.hadoop.hbase.regionserver.RSRpcServices.mutate(RSRpcServices.java:2751)
comment|//    at org.apache.hadoop.hbase.shaded.protobuf.generated.ClientProtos$ClientService$2.callBlockingMethod(ClientProtos.java:41546)
comment|//    at org.apache.hadoop.hbase.ipc.RpcServer.call(RpcServer.java:406)
comment|//    at org.apache.hadoop.hbase.ipc.CallRunner.run(CallRunner.java:133)
comment|//    at org.apache.hadoop.hbase.ipc.RpcExecutor$Handler.run(RpcExecutor.java:278)
comment|//    at org.apache.hadoop.hbase.ipc.RpcExecutor$Handler.run(RpcExecutor.java:258)
name|checkBalance
argument_list|(
name|SYSTEM_REGIONS
argument_list|,
name|REGIONS
operator|/
name|SLAVES
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|checkBalance
parameter_list|(
name|int
name|masterCount
parameter_list|,
name|int
name|rsCount
parameter_list|)
throws|throws
name|Exception
block|{
name|MiniHBaseCluster
name|cluster
init|=
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
name|MASTERS
argument_list|,
name|SLAVES
argument_list|)
decl_stmt|;
name|TableName
name|tn
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|Table
name|t
init|=
name|TEST_UTIL
operator|.
name|createMultiRegionTable
argument_list|(
name|tn
argument_list|,
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|REGIONS
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Server: "
operator|+
name|cluster
operator|.
name|getMaster
argument_list|()
operator|.
name|getServerManager
argument_list|()
operator|.
name|getOnlineServersList
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HRegion
argument_list|>
name|regions
init|=
name|cluster
operator|.
name|getMaster
argument_list|()
operator|.
name|getRegions
argument_list|()
decl_stmt|;
name|int
name|mActualCount
init|=
name|regions
operator|.
name|size
argument_list|()
decl_stmt|;
if|if
condition|(
name|masterCount
operator|==
literal|0
operator|||
name|masterCount
operator|==
name|SYSTEM_REGIONS
condition|)
block|{
comment|// 0 means no regions on master.
name|assertEquals
argument_list|(
name|masterCount
argument_list|,
name|mActualCount
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// This is master as a regionserver scenario.
name|checkCount
argument_list|(
name|masterCount
argument_list|,
name|mActualCount
argument_list|)
expr_stmt|;
block|}
comment|// Allow that balance is not exact. FYI, getRegionServerThreads does not include master
comment|// thread though it is a regionserver so we have to check master and then below the
comment|// regionservers.
for|for
control|(
name|JVMClusterUtil
operator|.
name|RegionServerThread
name|rst
range|:
name|cluster
operator|.
name|getRegionServerThreads
argument_list|()
control|)
block|{
name|regions
operator|=
name|rst
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getRegions
argument_list|()
expr_stmt|;
name|int
name|rsActualCount
init|=
name|regions
operator|.
name|size
argument_list|()
decl_stmt|;
name|checkCount
argument_list|(
name|rsActualCount
argument_list|,
name|rsCount
argument_list|)
expr_stmt|;
block|}
name|HMaster
name|oldMaster
init|=
name|cluster
operator|.
name|getMaster
argument_list|()
decl_stmt|;
name|cluster
operator|.
name|killMaster
argument_list|(
name|oldMaster
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
name|oldMaster
operator|.
name|join
argument_list|()
expr_stmt|;
while|while
condition|(
name|cluster
operator|.
name|getMaster
argument_list|()
operator|==
literal|null
operator|||
name|cluster
operator|.
name|getMaster
argument_list|()
operator|.
name|getServerName
argument_list|()
operator|.
name|equals
argument_list|(
name|oldMaster
operator|.
name|getServerName
argument_list|()
argument_list|)
condition|)
block|{
name|Threads
operator|.
name|sleep
argument_list|(
literal|10
argument_list|)
expr_stmt|;
block|}
while|while
condition|(
operator|!
name|cluster
operator|.
name|getMaster
argument_list|()
operator|.
name|isInitialized
argument_list|()
condition|)
block|{
name|Threads
operator|.
name|sleep
argument_list|(
literal|10
argument_list|)
expr_stmt|;
block|}
while|while
condition|(
name|cluster
operator|.
name|getMaster
argument_list|()
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|computeRegionInTransitionStat
argument_list|()
operator|.
name|getTotalRITs
argument_list|()
operator|>
literal|0
condition|)
block|{
name|Threads
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Waiting on RIT to go to zero before calling balancer..."
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Cluster is up; running balancer"
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|getMaster
argument_list|()
operator|.
name|balance
argument_list|()
expr_stmt|;
name|regions
operator|=
name|cluster
operator|.
name|getMaster
argument_list|()
operator|.
name|getRegions
argument_list|()
expr_stmt|;
name|int
name|mNewActualCount
init|=
name|regions
operator|.
name|size
argument_list|()
decl_stmt|;
if|if
condition|(
name|masterCount
operator|==
literal|0
operator|||
name|masterCount
operator|==
name|SYSTEM_REGIONS
condition|)
block|{
comment|// 0 means no regions on master. After crash, should still be no regions on master.
comment|// If masterCount == SYSTEM_REGIONS, means master only carrying system regions and should
comment|// still only carry system regions post crash.
name|assertEquals
argument_list|(
name|masterCount
argument_list|,
name|mNewActualCount
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Running shutdown of cluster"
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|checkCount
parameter_list|(
name|int
name|actual
parameter_list|,
name|int
name|expected
parameter_list|)
block|{
name|assertTrue
argument_list|(
literal|"Actual="
operator|+
name|actual
operator|+
literal|", expected="
operator|+
name|expected
argument_list|,
name|actual
operator|>=
operator|(
name|expected
operator|-
literal|2
operator|)
operator|&&
name|actual
operator|<=
operator|(
name|expected
operator|+
literal|2
operator|)
argument_list|)
expr_stmt|;
comment|// Lots of slop +/- 2
block|}
block|}
end_class

end_unit

