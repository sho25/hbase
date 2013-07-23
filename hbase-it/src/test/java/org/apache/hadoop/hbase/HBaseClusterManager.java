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
name|Map
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
name|lang
operator|.
name|StringUtils
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
name|HBaseClusterManager
operator|.
name|CommandProvider
operator|.
name|Operation
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
name|Pair
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
name|Shell
import|;
end_import

begin_comment
comment|/**  * A default cluster manager for HBase. Uses SSH, and hbase shell scripts  * to manage the cluster. Assumes Unix-like commands are available like 'ps',  * 'kill', etc. Also assumes the user running the test has enough "power" to start& stop  * servers on the remote machines (for example, the test user could be the same user as the  * user the daemon isrunning as)  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|HBaseClusterManager
extends|extends
name|ClusterManager
block|{
specifier|private
name|String
name|sshUserName
decl_stmt|;
specifier|private
name|String
name|sshOptions
decl_stmt|;
comment|/**    * The command format that is used to execute the remote command. Arguments:    * 1 SSH options, 2 user name , 3 "@" if username is set, 4 host, 5 original command.    */
specifier|private
specifier|static
specifier|final
name|String
name|DEFAULT_TUNNEL_CMD
init|=
literal|"/usr/bin/ssh %1$s %2$s%3$s%4$s \"%5$s\""
decl_stmt|;
specifier|private
name|String
name|tunnelCmd
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|super
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
if|if
condition|(
name|conf
operator|==
literal|null
condition|)
block|{
comment|// Configured gets passed null before real conf. Why? I don't know.
return|return;
block|}
name|sshUserName
operator|=
name|conf
operator|.
name|get
argument_list|(
literal|"hbase.it.clustermanager.ssh.user"
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|String
name|extraSshOptions
init|=
name|conf
operator|.
name|get
argument_list|(
literal|"hbase.it.clustermanager.ssh.opts"
argument_list|,
literal|""
argument_list|)
decl_stmt|;
name|sshOptions
operator|=
name|System
operator|.
name|getenv
argument_list|(
literal|"HBASE_SSH_OPTS"
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|extraSshOptions
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|sshOptions
operator|=
name|StringUtils
operator|.
name|join
argument_list|(
operator|new
name|Object
index|[]
block|{
name|sshOptions
block|,
name|extraSshOptions
block|}
argument_list|,
literal|" "
argument_list|)
expr_stmt|;
block|}
name|sshOptions
operator|=
operator|(
name|sshOptions
operator|==
literal|null
operator|)
condition|?
literal|""
else|:
name|sshOptions
expr_stmt|;
name|tunnelCmd
operator|=
name|conf
operator|.
name|get
argument_list|(
literal|"hbase.it.clustermanager.ssh.cmd"
argument_list|,
name|DEFAULT_TUNNEL_CMD
argument_list|)
expr_stmt|;
comment|// Print out ssh special config if any.
if|if
condition|(
operator|(
name|sshUserName
operator|!=
literal|null
operator|&&
name|sshUserName
operator|.
name|length
argument_list|()
operator|>
literal|0
operator|)
operator|||
operator|(
name|sshOptions
operator|!=
literal|null
operator|&&
name|sshOptions
operator|.
name|length
argument_list|()
operator|>
literal|0
operator|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Running with SSH user ["
operator|+
name|sshUserName
operator|+
literal|"] and options ["
operator|+
name|sshOptions
operator|+
literal|"]"
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Executes commands over SSH    */
specifier|protected
class|class
name|RemoteShell
extends|extends
name|Shell
operator|.
name|ShellCommandExecutor
block|{
specifier|private
name|String
name|hostname
decl_stmt|;
specifier|public
name|RemoteShell
parameter_list|(
name|String
name|hostname
parameter_list|,
name|String
index|[]
name|execString
parameter_list|,
name|File
name|dir
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|env
parameter_list|,
name|long
name|timeout
parameter_list|)
block|{
name|super
argument_list|(
name|execString
argument_list|,
name|dir
argument_list|,
name|env
argument_list|,
name|timeout
argument_list|)
expr_stmt|;
name|this
operator|.
name|hostname
operator|=
name|hostname
expr_stmt|;
block|}
specifier|public
name|RemoteShell
parameter_list|(
name|String
name|hostname
parameter_list|,
name|String
index|[]
name|execString
parameter_list|,
name|File
name|dir
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|env
parameter_list|)
block|{
name|super
argument_list|(
name|execString
argument_list|,
name|dir
argument_list|,
name|env
argument_list|)
expr_stmt|;
name|this
operator|.
name|hostname
operator|=
name|hostname
expr_stmt|;
block|}
specifier|public
name|RemoteShell
parameter_list|(
name|String
name|hostname
parameter_list|,
name|String
index|[]
name|execString
parameter_list|,
name|File
name|dir
parameter_list|)
block|{
name|super
argument_list|(
name|execString
argument_list|,
name|dir
argument_list|)
expr_stmt|;
name|this
operator|.
name|hostname
operator|=
name|hostname
expr_stmt|;
block|}
specifier|public
name|RemoteShell
parameter_list|(
name|String
name|hostname
parameter_list|,
name|String
index|[]
name|execString
parameter_list|)
block|{
name|super
argument_list|(
name|execString
argument_list|)
expr_stmt|;
name|this
operator|.
name|hostname
operator|=
name|hostname
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
index|[]
name|getExecString
parameter_list|()
block|{
name|String
name|at
init|=
name|sshUserName
operator|.
name|isEmpty
argument_list|()
condition|?
literal|""
else|:
literal|"@"
decl_stmt|;
name|String
name|remoteCmd
init|=
name|StringUtils
operator|.
name|join
argument_list|(
name|super
operator|.
name|getExecString
argument_list|()
argument_list|,
literal|" "
argument_list|)
decl_stmt|;
name|String
name|cmd
init|=
name|String
operator|.
name|format
argument_list|(
name|tunnelCmd
argument_list|,
name|sshOptions
argument_list|,
name|sshUserName
argument_list|,
name|at
argument_list|,
name|hostname
argument_list|,
name|remoteCmd
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Executing full command ["
operator|+
name|cmd
operator|+
literal|"]"
argument_list|)
expr_stmt|;
return|return
operator|new
name|String
index|[]
block|{
literal|"/usr/bin/env"
block|,
literal|"bash"
block|,
literal|"-c"
block|,
name|cmd
block|}
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|execute
parameter_list|()
throws|throws
name|IOException
block|{
name|super
operator|.
name|execute
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Provides command strings for services to be executed by Shell. CommandProviders are    * pluggable, and different deployments(windows, bigtop, etc) can be managed by    * plugging-in custom CommandProvider's or ClusterManager's.    */
specifier|static
specifier|abstract
class|class
name|CommandProvider
block|{
enum|enum
name|Operation
block|{
name|START
block|,
name|STOP
block|,
name|RESTART
block|}
specifier|public
specifier|abstract
name|String
name|getCommand
parameter_list|(
name|ServiceType
name|service
parameter_list|,
name|Operation
name|op
parameter_list|)
function_decl|;
specifier|public
name|String
name|isRunningCommand
parameter_list|(
name|ServiceType
name|service
parameter_list|)
block|{
return|return
name|findPidCommand
argument_list|(
name|service
argument_list|)
return|;
block|}
specifier|protected
name|String
name|findPidCommand
parameter_list|(
name|ServiceType
name|service
parameter_list|)
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"ps aux | grep proc_%s | grep -v grep | tr -s ' ' | cut -d ' ' -f2"
argument_list|,
name|service
argument_list|)
return|;
block|}
specifier|public
name|String
name|signalCommand
parameter_list|(
name|ServiceType
name|service
parameter_list|,
name|String
name|signal
parameter_list|)
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"%s | xargs kill -s %s"
argument_list|,
name|findPidCommand
argument_list|(
name|service
argument_list|)
argument_list|,
name|signal
argument_list|)
return|;
block|}
block|}
comment|/**    * CommandProvider to manage the service using bin/hbase-* scripts    */
specifier|static
class|class
name|HBaseShellCommandProvider
extends|extends
name|CommandProvider
block|{
specifier|private
specifier|final
name|String
name|hbaseHome
decl_stmt|;
specifier|private
specifier|final
name|String
name|confDir
decl_stmt|;
name|HBaseShellCommandProvider
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|hbaseHome
operator|=
name|conf
operator|.
name|get
argument_list|(
literal|"hbase.it.clustermanager.hbase.home"
argument_list|,
name|System
operator|.
name|getenv
argument_list|(
literal|"HBASE_HOME"
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|tmp
init|=
name|conf
operator|.
name|get
argument_list|(
literal|"hbase.it.clustermanager.hbase.conf.dir"
argument_list|,
name|System
operator|.
name|getenv
argument_list|(
literal|"HBASE_CONF_DIR"
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|tmp
operator|!=
literal|null
condition|)
block|{
name|confDir
operator|=
name|String
operator|.
name|format
argument_list|(
literal|"--config %s"
argument_list|,
name|tmp
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|confDir
operator|=
literal|""
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|getCommand
parameter_list|(
name|ServiceType
name|service
parameter_list|,
name|Operation
name|op
parameter_list|)
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"%s/bin/hbase-daemon.sh %s %s %s"
argument_list|,
name|hbaseHome
argument_list|,
name|confDir
argument_list|,
name|op
operator|.
name|toString
argument_list|()
operator|.
name|toLowerCase
argument_list|()
argument_list|,
name|service
argument_list|)
return|;
block|}
block|}
specifier|public
name|HBaseClusterManager
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|protected
name|CommandProvider
name|getCommandProvider
parameter_list|(
name|ServiceType
name|service
parameter_list|)
block|{
comment|//TODO: make it pluggable, or auto-detect the best command provider, should work with
comment|//hadoop daemons as well
return|return
operator|new
name|HBaseShellCommandProvider
argument_list|(
name|getConf
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Execute the given command on the host using SSH    * @return pair of exit code and command output    * @throws IOException if something goes wrong.    */
specifier|private
name|Pair
argument_list|<
name|Integer
argument_list|,
name|String
argument_list|>
name|exec
parameter_list|(
name|String
name|hostname
parameter_list|,
name|String
modifier|...
name|cmd
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Executing remote command: "
operator|+
name|StringUtils
operator|.
name|join
argument_list|(
name|cmd
argument_list|,
literal|" "
argument_list|)
operator|+
literal|" , hostname:"
operator|+
name|hostname
argument_list|)
expr_stmt|;
name|RemoteShell
name|shell
init|=
operator|new
name|RemoteShell
argument_list|(
name|hostname
argument_list|,
name|cmd
argument_list|)
decl_stmt|;
name|shell
operator|.
name|execute
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Executed remote command, exit code:"
operator|+
name|shell
operator|.
name|getExitCode
argument_list|()
operator|+
literal|" , output:"
operator|+
name|shell
operator|.
name|getOutput
argument_list|()
argument_list|)
expr_stmt|;
return|return
operator|new
name|Pair
argument_list|<
name|Integer
argument_list|,
name|String
argument_list|>
argument_list|(
name|shell
operator|.
name|getExitCode
argument_list|()
argument_list|,
name|shell
operator|.
name|getOutput
argument_list|()
argument_list|)
return|;
block|}
specifier|private
name|void
name|exec
parameter_list|(
name|String
name|hostname
parameter_list|,
name|ServiceType
name|service
parameter_list|,
name|Operation
name|op
parameter_list|)
throws|throws
name|IOException
block|{
name|exec
argument_list|(
name|hostname
argument_list|,
name|getCommandProvider
argument_list|(
name|service
argument_list|)
operator|.
name|getCommand
argument_list|(
name|service
argument_list|,
name|op
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|(
name|ServiceType
name|service
parameter_list|,
name|String
name|hostname
parameter_list|)
throws|throws
name|IOException
block|{
name|exec
argument_list|(
name|hostname
argument_list|,
name|service
argument_list|,
name|Operation
operator|.
name|START
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|ServiceType
name|service
parameter_list|,
name|String
name|hostname
parameter_list|)
throws|throws
name|IOException
block|{
name|exec
argument_list|(
name|hostname
argument_list|,
name|service
argument_list|,
name|Operation
operator|.
name|STOP
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|restart
parameter_list|(
name|ServiceType
name|service
parameter_list|,
name|String
name|hostname
parameter_list|)
throws|throws
name|IOException
block|{
name|exec
argument_list|(
name|hostname
argument_list|,
name|service
argument_list|,
name|Operation
operator|.
name|RESTART
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|signal
parameter_list|(
name|ServiceType
name|service
parameter_list|,
name|String
name|signal
parameter_list|,
name|String
name|hostname
parameter_list|)
throws|throws
name|IOException
block|{
name|exec
argument_list|(
name|hostname
argument_list|,
name|getCommandProvider
argument_list|(
name|service
argument_list|)
operator|.
name|signalCommand
argument_list|(
name|service
argument_list|,
name|signal
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isRunning
parameter_list|(
name|ServiceType
name|service
parameter_list|,
name|String
name|hostname
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|ret
init|=
name|exec
argument_list|(
name|hostname
argument_list|,
name|getCommandProvider
argument_list|(
name|service
argument_list|)
operator|.
name|isRunningCommand
argument_list|(
name|service
argument_list|)
argument_list|)
operator|.
name|getSecond
argument_list|()
decl_stmt|;
return|return
name|ret
operator|.
name|length
argument_list|()
operator|>
literal|0
return|;
block|}
block|}
end_class

end_unit

