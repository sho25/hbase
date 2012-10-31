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
name|util
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
name|lang
operator|.
name|reflect
operator|.
name|Constructor
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|InvocationTargetException
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
name|classification
operator|.
name|InterfaceAudience
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
name|fs
operator|.
name|FileSystem
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
name|regionserver
operator|.
name|HRegionServer
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
name|ShutdownHook
import|;
end_import

begin_comment
comment|/**  * Utility used running a cluster all in the one JVM.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|JVMClusterUtil
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
name|JVMClusterUtil
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Datastructure to hold RegionServer Thread and RegionServer instance    */
specifier|public
specifier|static
class|class
name|RegionServerThread
extends|extends
name|Thread
block|{
specifier|private
specifier|final
name|HRegionServer
name|regionServer
decl_stmt|;
specifier|public
name|RegionServerThread
parameter_list|(
specifier|final
name|HRegionServer
name|r
parameter_list|,
specifier|final
name|int
name|index
parameter_list|)
block|{
name|super
argument_list|(
name|r
argument_list|,
literal|"RegionServer:"
operator|+
name|index
operator|+
literal|";"
operator|+
name|r
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|regionServer
operator|=
name|r
expr_stmt|;
block|}
comment|/** @return the region server */
specifier|public
name|HRegionServer
name|getRegionServer
parameter_list|()
block|{
return|return
name|this
operator|.
name|regionServer
return|;
block|}
comment|/**      * Block until the region server has come online, indicating it is ready      * to be used.      */
specifier|public
name|void
name|waitForServerOnline
parameter_list|()
block|{
comment|// The server is marked online after the init method completes inside of
comment|// the HRS#run method.  HRS#init can fail for whatever region.  In those
comment|// cases, we'll jump out of the run without setting online flag.  Check
comment|// stopRequested so we don't wait here a flag that will never be flipped.
name|regionServer
operator|.
name|waitForServerOnline
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Creates a {@link RegionServerThread}.    * Call 'start' on the returned thread to make it run.    * @param c Configuration to use.    * @param hrsc Class to create.    * @param index Used distinguishing the object returned.    * @throws IOException    * @return Region server added.    */
specifier|public
specifier|static
name|JVMClusterUtil
operator|.
name|RegionServerThread
name|createRegionServerThread
parameter_list|(
specifier|final
name|Configuration
name|c
parameter_list|,
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|HRegionServer
argument_list|>
name|hrsc
parameter_list|,
specifier|final
name|int
name|index
parameter_list|)
throws|throws
name|IOException
block|{
name|HRegionServer
name|server
decl_stmt|;
try|try
block|{
name|Constructor
argument_list|<
name|?
extends|extends
name|HRegionServer
argument_list|>
name|ctor
init|=
name|hrsc
operator|.
name|getConstructor
argument_list|(
name|Configuration
operator|.
name|class
argument_list|)
decl_stmt|;
name|ctor
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|server
operator|=
name|ctor
operator|.
name|newInstance
argument_list|(
name|c
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InvocationTargetException
name|ite
parameter_list|)
block|{
name|Throwable
name|target
init|=
name|ite
operator|.
name|getTargetException
argument_list|()
decl_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Failed construction of RegionServer: "
operator|+
name|hrsc
operator|.
name|toString
argument_list|()
operator|+
operator|(
operator|(
name|target
operator|.
name|getCause
argument_list|()
operator|!=
literal|null
operator|)
condition|?
name|target
operator|.
name|getCause
argument_list|()
operator|.
name|getMessage
argument_list|()
else|:
literal|""
operator|)
argument_list|,
name|target
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|IOException
name|ioe
init|=
operator|new
name|IOException
argument_list|()
decl_stmt|;
name|ioe
operator|.
name|initCause
argument_list|(
name|e
argument_list|)
expr_stmt|;
throw|throw
name|ioe
throw|;
block|}
return|return
operator|new
name|JVMClusterUtil
operator|.
name|RegionServerThread
argument_list|(
name|server
argument_list|,
name|index
argument_list|)
return|;
block|}
comment|/**    * Datastructure to hold Master Thread and Master instance    */
specifier|public
specifier|static
class|class
name|MasterThread
extends|extends
name|Thread
block|{
specifier|private
specifier|final
name|HMaster
name|master
decl_stmt|;
specifier|public
name|MasterThread
parameter_list|(
specifier|final
name|HMaster
name|m
parameter_list|,
specifier|final
name|int
name|index
parameter_list|)
block|{
name|super
argument_list|(
name|m
argument_list|,
literal|"Master:"
operator|+
name|index
operator|+
literal|";"
operator|+
name|m
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|master
operator|=
name|m
expr_stmt|;
block|}
comment|/** @return the master */
specifier|public
name|HMaster
name|getMaster
parameter_list|()
block|{
return|return
name|this
operator|.
name|master
return|;
block|}
block|}
comment|/**    * Creates a {@link MasterThread}.    * Call 'start' on the returned thread to make it run.    * @param c Configuration to use.    * @param hmc Class to create.    * @param index Used distinguishing the object returned.    * @throws IOException    * @return Master added.    */
specifier|public
specifier|static
name|JVMClusterUtil
operator|.
name|MasterThread
name|createMasterThread
parameter_list|(
specifier|final
name|Configuration
name|c
parameter_list|,
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|HMaster
argument_list|>
name|hmc
parameter_list|,
specifier|final
name|int
name|index
parameter_list|)
throws|throws
name|IOException
block|{
name|HMaster
name|server
decl_stmt|;
try|try
block|{
name|server
operator|=
name|hmc
operator|.
name|getConstructor
argument_list|(
name|Configuration
operator|.
name|class
argument_list|)
operator|.
name|newInstance
argument_list|(
name|c
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InvocationTargetException
name|ite
parameter_list|)
block|{
name|Throwable
name|target
init|=
name|ite
operator|.
name|getTargetException
argument_list|()
decl_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Failed construction of Master: "
operator|+
name|hmc
operator|.
name|toString
argument_list|()
operator|+
operator|(
operator|(
name|target
operator|.
name|getCause
argument_list|()
operator|!=
literal|null
operator|)
condition|?
name|target
operator|.
name|getCause
argument_list|()
operator|.
name|getMessage
argument_list|()
else|:
literal|""
operator|)
argument_list|,
name|target
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|IOException
name|ioe
init|=
operator|new
name|IOException
argument_list|()
decl_stmt|;
name|ioe
operator|.
name|initCause
argument_list|(
name|e
argument_list|)
expr_stmt|;
throw|throw
name|ioe
throw|;
block|}
return|return
operator|new
name|JVMClusterUtil
operator|.
name|MasterThread
argument_list|(
name|server
argument_list|,
name|index
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|JVMClusterUtil
operator|.
name|MasterThread
name|findActiveMaster
parameter_list|(
name|List
argument_list|<
name|JVMClusterUtil
operator|.
name|MasterThread
argument_list|>
name|masters
parameter_list|)
block|{
for|for
control|(
name|JVMClusterUtil
operator|.
name|MasterThread
name|t
range|:
name|masters
control|)
block|{
if|if
condition|(
name|t
operator|.
name|master
operator|.
name|isActiveMaster
argument_list|()
condition|)
block|{
return|return
name|t
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
comment|/**    * Start the cluster.  Waits until there is a primary master initialized    * and returns its address.    * @param masters    * @param regionservers    * @return Address to use contacting primary master.    */
specifier|public
specifier|static
name|String
name|startup
parameter_list|(
specifier|final
name|List
argument_list|<
name|JVMClusterUtil
operator|.
name|MasterThread
argument_list|>
name|masters
parameter_list|,
specifier|final
name|List
argument_list|<
name|JVMClusterUtil
operator|.
name|RegionServerThread
argument_list|>
name|regionservers
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|masters
operator|==
literal|null
operator|||
name|masters
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
for|for
control|(
name|JVMClusterUtil
operator|.
name|MasterThread
name|t
range|:
name|masters
control|)
block|{
name|t
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
comment|// Wait for an active master
comment|//  having an active master before starting the region threads allows
comment|//  then to succeed on their connection to master
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
while|while
condition|(
name|findActiveMaster
argument_list|(
name|masters
argument_list|)
operator|==
literal|null
condition|)
block|{
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ignored
parameter_list|)
block|{       }
if|if
condition|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|>
name|startTime
operator|+
literal|30000
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Master not active after 30 seconds"
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
name|regionservers
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|JVMClusterUtil
operator|.
name|RegionServerThread
name|t
range|:
name|regionservers
control|)
block|{
name|HRegionServer
name|hrs
init|=
name|t
operator|.
name|getRegionServer
argument_list|()
decl_stmt|;
name|ShutdownHook
operator|.
name|install
argument_list|(
name|hrs
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|FileSystem
operator|.
name|get
argument_list|(
name|hrs
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|,
name|hrs
argument_list|,
name|t
argument_list|)
expr_stmt|;
name|t
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
block|}
comment|// Wait for an active master to be initialized (implies being master)
comment|//  with this, when we return the cluster is complete
name|startTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|JVMClusterUtil
operator|.
name|MasterThread
name|t
init|=
name|findActiveMaster
argument_list|(
name|masters
argument_list|)
decl_stmt|;
if|if
condition|(
name|t
operator|!=
literal|null
operator|&&
name|t
operator|.
name|master
operator|.
name|isInitialized
argument_list|()
condition|)
block|{
return|return
name|t
operator|.
name|master
operator|.
name|getServerName
argument_list|()
operator|.
name|toString
argument_list|()
return|;
block|}
if|if
condition|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|>
name|startTime
operator|+
literal|200000
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Master not initialized after 200 seconds"
argument_list|)
throw|;
block|}
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ignored
parameter_list|)
block|{
comment|// Keep waiting
block|}
block|}
block|}
comment|/**    * @param masters    * @param regionservers    */
specifier|public
specifier|static
name|void
name|shutdown
parameter_list|(
specifier|final
name|List
argument_list|<
name|MasterThread
argument_list|>
name|masters
parameter_list|,
specifier|final
name|List
argument_list|<
name|RegionServerThread
argument_list|>
name|regionservers
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Shutting down HBase Cluster"
argument_list|)
expr_stmt|;
if|if
condition|(
name|masters
operator|!=
literal|null
condition|)
block|{
comment|// Do backups first.
name|JVMClusterUtil
operator|.
name|MasterThread
name|activeMaster
init|=
literal|null
decl_stmt|;
for|for
control|(
name|JVMClusterUtil
operator|.
name|MasterThread
name|t
range|:
name|masters
control|)
block|{
if|if
condition|(
operator|!
name|t
operator|.
name|master
operator|.
name|isActiveMaster
argument_list|()
condition|)
block|{
name|t
operator|.
name|master
operator|.
name|stopMaster
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|activeMaster
operator|=
name|t
expr_stmt|;
block|}
block|}
comment|// Do active after.
if|if
condition|(
name|activeMaster
operator|!=
literal|null
condition|)
name|activeMaster
operator|.
name|master
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|regionservers
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|RegionServerThread
name|t
range|:
name|regionservers
control|)
block|{
if|if
condition|(
name|t
operator|.
name|isAlive
argument_list|()
condition|)
block|{
try|try
block|{
name|t
operator|.
name|getRegionServer
argument_list|()
operator|.
name|stop
argument_list|(
literal|"Shutdown requested"
argument_list|)
expr_stmt|;
name|t
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// continue
block|}
block|}
block|}
block|}
if|if
condition|(
name|masters
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|JVMClusterUtil
operator|.
name|MasterThread
name|t
range|:
name|masters
control|)
block|{
while|while
condition|(
name|t
operator|.
name|master
operator|.
name|isAlive
argument_list|()
condition|)
block|{
try|try
block|{
comment|// The below has been replaced to debug sometime hangs on end of
comment|// tests.
comment|// this.master.join():
name|Threads
operator|.
name|threadDumpingIsAlive
argument_list|(
name|t
operator|.
name|master
operator|.
name|getThread
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// continue
block|}
block|}
block|}
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Shutdown of "
operator|+
operator|(
operator|(
name|masters
operator|!=
literal|null
operator|)
condition|?
name|masters
operator|.
name|size
argument_list|()
else|:
literal|"0"
operator|)
operator|+
literal|" master(s) and "
operator|+
operator|(
operator|(
name|regionservers
operator|!=
literal|null
operator|)
condition|?
name|regionservers
operator|.
name|size
argument_list|()
else|:
literal|"0"
operator|)
operator|+
literal|" regionserver(s) complete"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

