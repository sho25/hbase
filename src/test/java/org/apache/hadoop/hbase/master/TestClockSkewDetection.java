begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|junit
operator|.
name|framework
operator|.
name|Assert
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
name|*
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
name|catalog
operator|.
name|CatalogTracker
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
name|SmallTests
operator|.
name|class
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
name|Server
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|CatalogTracker
name|getCatalogTracker
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|Configuration
name|getConfiguration
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
annotation|@
name|Override
specifier|public
name|ServerName
name|getServerName
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|ZooKeeperWatcher
name|getZooKeeper
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|abort
parameter_list|(
name|String
name|why
parameter_list|,
name|Throwable
name|e
parameter_list|)
block|{}
annotation|@
name|Override
specifier|public
name|boolean
name|isAborted
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isStopped
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|String
name|why
parameter_list|)
block|{       }
block|}
argument_list|,
literal|null
argument_list|,
literal|false
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
name|sm
operator|.
name|regionServerStartup
argument_list|(
name|ia1
argument_list|,
literal|1234
argument_list|,
operator|-
literal|1
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
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
name|sm
operator|.
name|regionServerStartup
argument_list|(
name|ia2
argument_list|,
literal|1235
argument_list|,
operator|-
literal|1
argument_list|,
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
name|fail
argument_list|(
literal|"HMaster should have thrown an ClockOutOfSyncException but didn't."
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
literal|"Recieved expected exception: "
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
name|sm
operator|.
name|regionServerStartup
argument_list|(
name|ia3
argument_list|,
literal|1236
argument_list|,
operator|-
literal|1
argument_list|,
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
block|}
annotation|@
name|org
operator|.
name|junit
operator|.
name|Rule
specifier|public
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ResourceCheckerJUnitRule
name|cu
init|=
operator|new
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ResourceCheckerJUnitRule
argument_list|()
decl_stmt|;
block|}
end_class

end_unit

