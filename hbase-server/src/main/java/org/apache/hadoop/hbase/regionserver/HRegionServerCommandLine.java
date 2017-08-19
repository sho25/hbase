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
name|regionserver
package|;
end_package

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
name|CoordinatedStateManagerFactory
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
name|util
operator|.
name|ServerCommandLine
import|;
end_import

begin_comment
comment|/**  * Class responsible for parsing the command line and starting the  * RegionServer.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|HRegionServerCommandLine
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
name|HRegionServerCommandLine
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|HRegionServer
argument_list|>
name|regionServerClass
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|USAGE
init|=
literal|"Usage: HRegionServer [-D conf.param=value] start"
decl_stmt|;
specifier|public
name|HRegionServerCommandLine
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|HRegionServer
argument_list|>
name|clazz
parameter_list|)
block|{
name|this
operator|.
name|regionServerClass
operator|=
name|clazz
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
specifier|private
name|int
name|start
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|getConf
argument_list|()
decl_stmt|;
name|CoordinatedStateManager
name|cp
init|=
name|CoordinatedStateManagerFactory
operator|.
name|getCoordinatedStateManager
argument_list|(
name|conf
argument_list|)
decl_stmt|;
try|try
block|{
comment|// If 'local', don't start a region server here. Defer to
comment|// LocalHBaseCluster. It manages 'local' clusters.
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
name|LOG
operator|.
name|warn
argument_list|(
literal|"Not starting a distinct region server because "
operator|+
name|HConstants
operator|.
name|CLUSTER_DISTRIBUTED
operator|+
literal|" is false"
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
name|HRegionServer
name|hrs
init|=
name|HRegionServer
operator|.
name|constructRegionServer
argument_list|(
name|regionServerClass
argument_list|,
name|conf
argument_list|,
name|cp
argument_list|)
decl_stmt|;
name|hrs
operator|.
name|start
argument_list|()
expr_stmt|;
name|hrs
operator|.
name|join
argument_list|()
expr_stmt|;
if|if
condition|(
name|hrs
operator|.
name|isAborted
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"HRegionServer Aborted"
argument_list|)
throw|;
block|}
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
literal|"Region server exiting"
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
if|if
condition|(
name|args
operator|.
name|length
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
name|cmd
init|=
name|args
index|[
literal|0
index|]
decl_stmt|;
if|if
condition|(
literal|"start"
operator|.
name|equals
argument_list|(
name|cmd
argument_list|)
condition|)
block|{
return|return
name|start
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
name|cmd
argument_list|)
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"To shutdown the regionserver run "
operator|+
literal|"hbase-daemon.sh stop regionserver or send a kill signal to "
operator|+
literal|"the regionserver pid"
argument_list|)
expr_stmt|;
return|return
literal|1
return|;
block|}
else|else
block|{
name|usage
argument_list|(
literal|"Unknown command: "
operator|+
name|args
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
return|return
literal|1
return|;
block|}
block|}
block|}
end_class

end_unit

