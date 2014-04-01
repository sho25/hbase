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
import|import
name|java
operator|.
name|io
operator|.
name|File
import|;
end_import

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
name|cli
operator|.
name|GnuParser
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
name|Options
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
name|ParseException
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
name|hbase
operator|.
name|MasterNotRunningException
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
name|ZNodeClearer
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
name|LocalHBaseCluster
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
name|ZooKeeperConnectionException
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
name|ServerCommandLine
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
name|MiniZooKeeperCluster
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
name|metrics2
operator|.
name|lib
operator|.
name|DefaultMetricsSystem
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
name|KeeperException
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|HMasterCommandLine
extends|extends
name|ServerCommandLine
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
name|HMasterCommandLine
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|USAGE
init|=
literal|"Usage: Master [opts] start|stop|clear\n"
operator|+
literal|" start  Start Master. If local mode, start Master and RegionServer in same JVM\n"
operator|+
literal|" stop   Start cluster shutdown; Master signals RegionServer shutdown\n"
operator|+
literal|" clear  Delete the master znode in ZooKeeper after a master crashes\n "
operator|+
literal|" where [opts] are:\n"
operator|+
literal|"   --minRegionServers=<servers>   Minimum RegionServers needed to host user tables.\n"
operator|+
literal|"   --localRegionServers=<servers> "
operator|+
literal|"RegionServers to start in master process when in standalone mode.\n"
operator|+
literal|"   --masters=<servers>            Masters to start in this process.\n"
operator|+
literal|"   --backup                       Master should start in backup mode"
decl_stmt|;
specifier|private
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|HMaster
argument_list|>
name|masterClass
decl_stmt|;
specifier|public
name|HMasterCommandLine
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|HMaster
argument_list|>
name|masterClass
parameter_list|)
block|{
name|this
operator|.
name|masterClass
operator|=
name|masterClass
expr_stmt|;
block|}
specifier|protected
name|String
name|getUsage
parameter_list|()
block|{
return|return
name|USAGE
return|;
block|}
specifier|public
name|int
name|run
parameter_list|(
name|String
name|args
index|[]
parameter_list|)
throws|throws
name|Exception
block|{
name|Options
name|opt
init|=
operator|new
name|Options
argument_list|()
decl_stmt|;
name|opt
operator|.
name|addOption
argument_list|(
literal|"localRegionServers"
argument_list|,
literal|true
argument_list|,
literal|"RegionServers to start in master process when running standalone"
argument_list|)
expr_stmt|;
name|opt
operator|.
name|addOption
argument_list|(
literal|"masters"
argument_list|,
literal|true
argument_list|,
literal|"Masters to start in this process"
argument_list|)
expr_stmt|;
name|opt
operator|.
name|addOption
argument_list|(
literal|"minRegionServers"
argument_list|,
literal|true
argument_list|,
literal|"Minimum RegionServers needed to host user tables"
argument_list|)
expr_stmt|;
name|opt
operator|.
name|addOption
argument_list|(
literal|"backup"
argument_list|,
literal|false
argument_list|,
literal|"Do not try to become HMaster until the primary fails"
argument_list|)
expr_stmt|;
name|CommandLine
name|cmd
decl_stmt|;
try|try
block|{
name|cmd
operator|=
operator|new
name|GnuParser
argument_list|()
operator|.
name|parse
argument_list|(
name|opt
argument_list|,
name|args
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ParseException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Could not parse: "
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|usage
argument_list|(
literal|null
argument_list|)
expr_stmt|;
return|return
literal|1
return|;
block|}
if|if
condition|(
name|cmd
operator|.
name|hasOption
argument_list|(
literal|"minRegionServers"
argument_list|)
condition|)
block|{
name|String
name|val
init|=
name|cmd
operator|.
name|getOptionValue
argument_list|(
literal|"minRegionServers"
argument_list|)
decl_stmt|;
name|getConf
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.regions.server.count.min"
argument_list|,
name|Integer
operator|.
name|valueOf
argument_list|(
name|val
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"minRegionServers set to "
operator|+
name|val
argument_list|)
expr_stmt|;
block|}
comment|// minRegionServers used to be minServers.  Support it too.
if|if
condition|(
name|cmd
operator|.
name|hasOption
argument_list|(
literal|"minServers"
argument_list|)
condition|)
block|{
name|String
name|val
init|=
name|cmd
operator|.
name|getOptionValue
argument_list|(
literal|"minServers"
argument_list|)
decl_stmt|;
name|getConf
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.regions.server.count.min"
argument_list|,
name|Integer
operator|.
name|valueOf
argument_list|(
name|val
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"minServers set to "
operator|+
name|val
argument_list|)
expr_stmt|;
block|}
comment|// check if we are the backup master - override the conf if so
if|if
condition|(
name|cmd
operator|.
name|hasOption
argument_list|(
literal|"backup"
argument_list|)
condition|)
block|{
name|getConf
argument_list|()
operator|.
name|setBoolean
argument_list|(
name|HConstants
operator|.
name|MASTER_TYPE_BACKUP
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|// How many regionservers to startup in this process (we run regionservers in same process as
comment|// master when we are in local/standalone mode. Useful testing)
if|if
condition|(
name|cmd
operator|.
name|hasOption
argument_list|(
literal|"localRegionServers"
argument_list|)
condition|)
block|{
name|String
name|val
init|=
name|cmd
operator|.
name|getOptionValue
argument_list|(
literal|"localRegionServers"
argument_list|)
decl_stmt|;
name|getConf
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.regionservers"
argument_list|,
name|Integer
operator|.
name|valueOf
argument_list|(
name|val
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"localRegionServers set to "
operator|+
name|val
argument_list|)
expr_stmt|;
block|}
comment|// How many masters to startup inside this process; useful testing
if|if
condition|(
name|cmd
operator|.
name|hasOption
argument_list|(
literal|"masters"
argument_list|)
condition|)
block|{
name|String
name|val
init|=
name|cmd
operator|.
name|getOptionValue
argument_list|(
literal|"masters"
argument_list|)
decl_stmt|;
name|getConf
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.masters"
argument_list|,
name|Integer
operator|.
name|valueOf
argument_list|(
name|val
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"masters set to "
operator|+
name|val
argument_list|)
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|List
argument_list|<
name|String
argument_list|>
name|remainingArgs
init|=
name|cmd
operator|.
name|getArgList
argument_list|()
decl_stmt|;
if|if
condition|(
name|remainingArgs
operator|.
name|size
argument_list|()
operator|!=
literal|1
condition|)
block|{
name|usage
argument_list|(
literal|null
argument_list|)
expr_stmt|;
return|return
literal|1
return|;
block|}
name|String
name|command
init|=
name|remainingArgs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
if|if
condition|(
literal|"start"
operator|.
name|equals
argument_list|(
name|command
argument_list|)
condition|)
block|{
return|return
name|startMaster
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
literal|"stop"
operator|.
name|equals
argument_list|(
name|command
argument_list|)
condition|)
block|{
return|return
name|stopMaster
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
literal|"clear"
operator|.
name|equals
argument_list|(
name|command
argument_list|)
condition|)
block|{
return|return
operator|(
name|ZNodeClearer
operator|.
name|clear
argument_list|(
name|getConf
argument_list|()
argument_list|)
condition|?
literal|0
else|:
literal|1
operator|)
return|;
block|}
else|else
block|{
name|usage
argument_list|(
literal|"Invalid command: "
operator|+
name|command
argument_list|)
expr_stmt|;
return|return
literal|1
return|;
block|}
block|}
specifier|private
name|int
name|startMaster
parameter_list|()
block|{
name|Configuration
name|conf
init|=
name|getConf
argument_list|()
decl_stmt|;
try|try
block|{
comment|// If 'local', defer to LocalHBaseCluster instance.  Starts master
comment|// and regionserver both in the one JVM.
if|if
condition|(
name|LocalHBaseCluster
operator|.
name|isLocal
argument_list|(
name|conf
argument_list|)
condition|)
block|{
name|DefaultMetricsSystem
operator|.
name|setMiniClusterMode
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|ServerManager
operator|.
name|WAIT_ON_REGIONSERVERS_MINTOSTART
argument_list|,
literal|1
argument_list|)
expr_stmt|;
specifier|final
name|MiniZooKeeperCluster
name|zooKeeperCluster
init|=
operator|new
name|MiniZooKeeperCluster
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|File
name|zkDataPath
init|=
operator|new
name|File
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_DATA_DIR
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|zkClientPort
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_CLIENT_PORT
argument_list|,
literal|0
argument_list|)
decl_stmt|;
if|if
condition|(
name|zkClientPort
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"No config value for "
operator|+
name|HConstants
operator|.
name|ZOOKEEPER_CLIENT_PORT
argument_list|)
throw|;
block|}
name|zooKeeperCluster
operator|.
name|setDefaultClientPort
argument_list|(
name|zkClientPort
argument_list|)
expr_stmt|;
comment|// login the zookeeper server principal (if using security)
name|ZKUtil
operator|.
name|loginServer
argument_list|(
name|conf
argument_list|,
literal|"hbase.zookeeper.server.keytab.file"
argument_list|,
literal|"hbase.zookeeper.server.kerberos.principal"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|int
name|clientPort
init|=
name|zooKeeperCluster
operator|.
name|startup
argument_list|(
name|zkDataPath
argument_list|)
decl_stmt|;
if|if
condition|(
name|clientPort
operator|!=
name|zkClientPort
condition|)
block|{
name|String
name|errorMsg
init|=
literal|"Could not start ZK at requested port of "
operator|+
name|zkClientPort
operator|+
literal|".  ZK was started at port: "
operator|+
name|clientPort
operator|+
literal|".  Aborting as clients (e.g. shell) will not be able to find "
operator|+
literal|"this ZK quorum."
decl_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
name|errorMsg
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
name|errorMsg
argument_list|)
throw|;
block|}
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_CLIENT_PORT
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|clientPort
argument_list|)
argument_list|)
expr_stmt|;
comment|// Need to have the zk cluster shutdown when master is shutdown.
comment|// Run a subclass that does the zk cluster shutdown on its way out.
name|LocalHBaseCluster
name|cluster
init|=
operator|new
name|LocalHBaseCluster
argument_list|(
name|conf
argument_list|,
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.masters"
argument_list|,
literal|1
argument_list|)
argument_list|,
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.regionservers"
argument_list|,
literal|0
argument_list|)
argument_list|,
name|LocalHMaster
operator|.
name|class
argument_list|,
name|HRegionServer
operator|.
name|class
argument_list|)
decl_stmt|;
operator|(
operator|(
name|LocalHMaster
operator|)
name|cluster
operator|.
name|getMaster
argument_list|(
literal|0
argument_list|)
operator|)
operator|.
name|setZKCluster
argument_list|(
name|zooKeeperCluster
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|startup
argument_list|()
expr_stmt|;
name|waitOnMasterThreads
argument_list|(
name|cluster
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|logProcessInfo
argument_list|(
name|getConf
argument_list|()
argument_list|)
expr_stmt|;
name|HMaster
name|master
init|=
name|HMaster
operator|.
name|constructMaster
argument_list|(
name|masterClass
argument_list|,
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
name|master
operator|.
name|isStopped
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Won't bring the Master up as a shutdown is requested"
argument_list|)
expr_stmt|;
return|return
literal|1
return|;
block|}
name|master
operator|.
name|start
argument_list|()
expr_stmt|;
name|master
operator|.
name|join
argument_list|()
expr_stmt|;
if|if
condition|(
name|master
operator|.
name|isAborted
argument_list|()
condition|)
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"HMaster Aborted"
argument_list|)
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Master exiting"
argument_list|,
name|t
argument_list|)
expr_stmt|;
return|return
literal|1
return|;
block|}
return|return
literal|0
return|;
block|}
specifier|private
name|int
name|stopMaster
parameter_list|()
block|{
name|HBaseAdmin
name|adm
init|=
literal|null
decl_stmt|;
try|try
block|{
name|Configuration
name|conf
init|=
name|getConf
argument_list|()
decl_stmt|;
comment|// Don't try more than once
name|conf
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_RETRIES_NUMBER
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|adm
operator|=
operator|new
name|HBaseAdmin
argument_list|(
name|getConf
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MasterNotRunningException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Master not running"
argument_list|)
expr_stmt|;
return|return
literal|1
return|;
block|}
catch|catch
parameter_list|(
name|ZooKeeperConnectionException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"ZooKeeper not available"
argument_list|)
expr_stmt|;
return|return
literal|1
return|;
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
literal|"Got IOException: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
literal|1
return|;
block|}
try|try
block|{
name|adm
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to stop master"
argument_list|,
name|t
argument_list|)
expr_stmt|;
return|return
literal|1
return|;
block|}
return|return
literal|0
return|;
block|}
specifier|private
name|void
name|waitOnMasterThreads
parameter_list|(
name|LocalHBaseCluster
name|cluster
parameter_list|)
throws|throws
name|InterruptedException
block|{
name|List
argument_list|<
name|JVMClusterUtil
operator|.
name|MasterThread
argument_list|>
name|masters
init|=
name|cluster
operator|.
name|getMasters
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|JVMClusterUtil
operator|.
name|RegionServerThread
argument_list|>
name|regionservers
init|=
name|cluster
operator|.
name|getRegionServers
argument_list|()
decl_stmt|;
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
name|t
operator|.
name|join
argument_list|()
expr_stmt|;
if|if
condition|(
name|t
operator|.
name|getMaster
argument_list|()
operator|.
name|isAborted
argument_list|()
condition|)
block|{
name|closeAllRegionServerThreads
argument_list|(
name|regionservers
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"HMaster Aborted"
argument_list|)
throw|;
block|}
block|}
block|}
block|}
specifier|private
specifier|static
name|void
name|closeAllRegionServerThreads
parameter_list|(
name|List
argument_list|<
name|JVMClusterUtil
operator|.
name|RegionServerThread
argument_list|>
name|regionservers
parameter_list|)
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
name|t
operator|.
name|getRegionServer
argument_list|()
operator|.
name|stop
argument_list|(
literal|"HMaster Aborted; Bringing down regions servers"
argument_list|)
expr_stmt|;
block|}
block|}
comment|/*    * Version of master that will shutdown the passed zk cluster on its way out.    */
specifier|public
specifier|static
class|class
name|LocalHMaster
extends|extends
name|HMaster
block|{
specifier|private
name|MiniZooKeeperCluster
name|zkcluster
init|=
literal|null
decl_stmt|;
specifier|public
name|LocalHMaster
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
throws|,
name|KeeperException
throws|,
name|InterruptedException
block|{
name|super
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|super
operator|.
name|run
argument_list|()
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|zkcluster
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|this
operator|.
name|zkcluster
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
block|}
name|void
name|setZKCluster
parameter_list|(
specifier|final
name|MiniZooKeeperCluster
name|zkcluster
parameter_list|)
block|{
name|this
operator|.
name|zkcluster
operator|=
name|zkcluster
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

