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
operator|.
name|master
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
name|fail
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|mock
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|when
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|InetAddress
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
name|ChoreService
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
name|ClockOutOfSyncException
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
name|CoordinatedStateManager
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
name|HBaseConfiguration
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
name|Server
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
name|client
operator|.
name|ClusterConnection
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
name|RpcControllerFactory
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|RegionServerStatusProtos
operator|.
name|RegionServerStartupRequest
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
name|SmallTests
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
name|MetaTableLocator
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

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MasterTests
operator|.
name|class
block|,
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestClockSkewDetection
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
name|TestClockSkewDetection
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testClockSkewDetection
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|ServerManager
name|sm
init|=
operator|new
name|ServerManager
argument_list|(
operator|new
name|MockNoopMasterServices
argument_list|(
name|conf
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|ClusterConnection
name|getClusterConnection
parameter_list|()
block|{
name|ClusterConnection
name|conn
init|=
name|mock
argument_list|(
name|ClusterConnection
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|conn
operator|.
name|getRpcControllerFactory
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|mock
argument_list|(
name|RpcControllerFactory
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|conn
return|;
block|}
block|}
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"regionServerStartup 1"
argument_list|)
expr_stmt|;
name|InetAddress
name|ia1
init|=
name|InetAddress
operator|.
name|getLocalHost
argument_list|()
decl_stmt|;
name|RegionServerStartupRequest
operator|.
name|Builder
name|request
init|=
name|RegionServerStartupRequest
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|request
operator|.
name|setPort
argument_list|(
literal|1234
argument_list|)
expr_stmt|;
name|request
operator|.
name|setServerStartCode
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
name|request
operator|.
name|setServerCurrentTime
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
expr_stmt|;
name|sm
operator|.
name|regionServerStartup
argument_list|(
name|request
operator|.
name|build
argument_list|()
argument_list|,
name|ia1
argument_list|)
expr_stmt|;
specifier|final
name|Configuration
name|c
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|long
name|maxSkew
init|=
name|c
operator|.
name|getLong
argument_list|(
literal|"hbase.master.maxclockskew"
argument_list|,
literal|30000
argument_list|)
decl_stmt|;
name|long
name|warningSkew
init|=
name|c
operator|.
name|getLong
argument_list|(
literal|"hbase.master.warningclockskew"
argument_list|,
literal|1000
argument_list|)
decl_stmt|;
try|try
block|{
comment|//Master Time> Region Server Time
name|LOG
operator|.
name|debug
argument_list|(
literal|"Test: Master Time> Region Server Time"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"regionServerStartup 2"
argument_list|)
expr_stmt|;
name|InetAddress
name|ia2
init|=
name|InetAddress
operator|.
name|getLocalHost
argument_list|()
decl_stmt|;
name|request
operator|=
name|RegionServerStartupRequest
operator|.
name|newBuilder
argument_list|()
expr_stmt|;
name|request
operator|.
name|setPort
argument_list|(
literal|1235
argument_list|)
expr_stmt|;
name|request
operator|.
name|setServerStartCode
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
name|request
operator|.
name|setServerCurrentTime
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|maxSkew
operator|*
literal|2
argument_list|)
expr_stmt|;
name|sm
operator|.
name|regionServerStartup
argument_list|(
name|request
operator|.
name|build
argument_list|()
argument_list|,
name|ia2
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"HMaster should have thrown a ClockOutOfSyncException but didn't."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClockOutOfSyncException
name|e
parameter_list|)
block|{
comment|//we want an exception
name|LOG
operator|.
name|info
argument_list|(
literal|"Received expected exception: "
operator|+
name|e
argument_list|)
expr_stmt|;
block|}
try|try
block|{
comment|// Master Time< Region Server Time
name|LOG
operator|.
name|debug
argument_list|(
literal|"Test: Master Time< Region Server Time"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"regionServerStartup 3"
argument_list|)
expr_stmt|;
name|InetAddress
name|ia3
init|=
name|InetAddress
operator|.
name|getLocalHost
argument_list|()
decl_stmt|;
name|request
operator|=
name|RegionServerStartupRequest
operator|.
name|newBuilder
argument_list|()
expr_stmt|;
name|request
operator|.
name|setPort
argument_list|(
literal|1236
argument_list|)
expr_stmt|;
name|request
operator|.
name|setServerStartCode
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
name|request
operator|.
name|setServerCurrentTime
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|+
name|maxSkew
operator|*
literal|2
argument_list|)
expr_stmt|;
name|sm
operator|.
name|regionServerStartup
argument_list|(
name|request
operator|.
name|build
argument_list|()
argument_list|,
name|ia3
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"HMaster should have thrown a ClockOutOfSyncException but didn't."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClockOutOfSyncException
name|e
parameter_list|)
block|{
comment|// we want an exception
name|LOG
operator|.
name|info
argument_list|(
literal|"Received expected exception: "
operator|+
name|e
argument_list|)
expr_stmt|;
block|}
comment|// make sure values above warning threshold but below max threshold don't kill
name|LOG
operator|.
name|debug
argument_list|(
literal|"regionServerStartup 4"
argument_list|)
expr_stmt|;
name|InetAddress
name|ia4
init|=
name|InetAddress
operator|.
name|getLocalHost
argument_list|()
decl_stmt|;
name|request
operator|=
name|RegionServerStartupRequest
operator|.
name|newBuilder
argument_list|()
expr_stmt|;
name|request
operator|.
name|setPort
argument_list|(
literal|1237
argument_list|)
expr_stmt|;
name|request
operator|.
name|setServerStartCode
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
name|request
operator|.
name|setServerCurrentTime
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|warningSkew
operator|*
literal|2
argument_list|)
expr_stmt|;
name|sm
operator|.
name|regionServerStartup
argument_list|(
name|request
operator|.
name|build
argument_list|()
argument_list|,
name|ia4
argument_list|)
expr_stmt|;
comment|// make sure values above warning threshold but below max threshold don't kill
name|LOG
operator|.
name|debug
argument_list|(
literal|"regionServerStartup 5"
argument_list|)
expr_stmt|;
name|InetAddress
name|ia5
init|=
name|InetAddress
operator|.
name|getLocalHost
argument_list|()
decl_stmt|;
name|request
operator|=
name|RegionServerStartupRequest
operator|.
name|newBuilder
argument_list|()
expr_stmt|;
name|request
operator|.
name|setPort
argument_list|(
literal|1238
argument_list|)
expr_stmt|;
name|request
operator|.
name|setServerStartCode
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
name|request
operator|.
name|setServerCurrentTime
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|+
name|warningSkew
operator|*
literal|2
argument_list|)
expr_stmt|;
name|sm
operator|.
name|regionServerStartup
argument_list|(
name|request
operator|.
name|build
argument_list|()
argument_list|,
name|ia5
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

