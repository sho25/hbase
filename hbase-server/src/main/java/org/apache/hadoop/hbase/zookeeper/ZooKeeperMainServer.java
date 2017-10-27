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
name|zookeeper
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
name|curator
operator|.
name|shaded
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
name|HBaseInterfaceAudience
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
name|zookeeper
operator|.
name|KeeperException
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
name|ZooKeeperMain
import|;
end_import

begin_comment
comment|/**  * Tool for running ZookeeperMain from HBase by  reading a ZooKeeper server  * from HBase XML configuration.  */
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
name|ZooKeeperMainServer
block|{
specifier|private
specifier|static
specifier|final
name|String
name|SERVER_ARG
init|=
literal|"-server"
decl_stmt|;
specifier|public
name|String
name|parse
parameter_list|(
specifier|final
name|Configuration
name|c
parameter_list|)
block|{
return|return
name|ZKConfig
operator|.
name|getZKQuorumServersString
argument_list|(
name|c
argument_list|)
return|;
block|}
comment|/**    * ZooKeeper 3.4.6 broke being able to pass commands on command line.    * See ZOOKEEPER-1897.  This class is a hack to restore this faclity.    */
specifier|private
specifier|static
class|class
name|HACK_UNTIL_ZOOKEEPER_1897_ZooKeeperMain
extends|extends
name|ZooKeeperMain
block|{
specifier|public
name|HACK_UNTIL_ZOOKEEPER_1897_ZooKeeperMain
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|super
argument_list|(
name|args
argument_list|)
expr_stmt|;
comment|// Make sure we are connected before we proceed. Can take a while on some systems. If we
comment|// run the command without being connected, we get ConnectionLoss KeeperErrorConnection...
name|Stopwatch
name|stopWatch
init|=
name|Stopwatch
operator|.
name|createStarted
argument_list|()
decl_stmt|;
while|while
condition|(
operator|!
name|this
operator|.
name|zk
operator|.
name|getState
argument_list|()
operator|.
name|isConnected
argument_list|()
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|1
argument_list|)
expr_stmt|;
if|if
condition|(
name|stopWatch
operator|.
name|elapsed
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
operator|>
literal|10
condition|)
block|{
throw|throw
operator|new
name|InterruptedException
argument_list|(
literal|"Failed connect "
operator|+
name|this
operator|.
name|zk
argument_list|)
throw|;
block|}
block|}
block|}
comment|/**      * Run the command-line args passed.  Calls System.exit when done.      * @throws KeeperException      * @throws IOException      * @throws InterruptedException      */
name|void
name|runCmdLine
parameter_list|()
throws|throws
name|KeeperException
throws|,
name|IOException
throws|,
name|InterruptedException
block|{
name|processCmd
argument_list|(
name|this
operator|.
name|cl
argument_list|)
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * @param args    * @return True if argument strings have a '-server' in them.    */
specifier|private
specifier|static
name|boolean
name|hasServer
parameter_list|(
specifier|final
name|String
name|args
index|[]
parameter_list|)
block|{
return|return
name|args
operator|.
name|length
operator|>
literal|0
operator|&&
name|args
index|[
literal|0
index|]
operator|.
name|equals
argument_list|(
name|SERVER_ARG
argument_list|)
return|;
block|}
comment|/**    * @param args    * @return True if command-line arguments were passed.    */
specifier|private
specifier|static
name|boolean
name|hasCommandLineArguments
parameter_list|(
specifier|final
name|String
name|args
index|[]
parameter_list|)
block|{
if|if
condition|(
name|hasServer
argument_list|(
name|args
argument_list|)
condition|)
block|{
if|if
condition|(
name|args
operator|.
name|length
operator|<
literal|2
condition|)
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"-server param but no value"
argument_list|)
throw|;
return|return
name|args
operator|.
name|length
operator|>
literal|2
return|;
block|}
return|return
name|args
operator|.
name|length
operator|>
literal|0
return|;
block|}
comment|/**    * Run the tool.    * @param args Command line arguments. First arg is path to zookeepers file.    */
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
name|args
index|[]
parameter_list|)
throws|throws
name|Exception
block|{
name|String
index|[]
name|newArgs
init|=
name|args
decl_stmt|;
if|if
condition|(
operator|!
name|hasServer
argument_list|(
name|args
argument_list|)
condition|)
block|{
comment|// Add the zk ensemble from configuration if none passed on command-line.
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|String
name|hostport
init|=
operator|new
name|ZooKeeperMainServer
argument_list|()
operator|.
name|parse
argument_list|(
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
name|hostport
operator|!=
literal|null
operator|&&
name|hostport
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|newArgs
operator|=
operator|new
name|String
index|[
name|args
operator|.
name|length
operator|+
literal|2
index|]
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|args
argument_list|,
literal|0
argument_list|,
name|newArgs
argument_list|,
literal|2
argument_list|,
name|args
operator|.
name|length
argument_list|)
expr_stmt|;
name|newArgs
index|[
literal|0
index|]
operator|=
literal|"-server"
expr_stmt|;
name|newArgs
index|[
literal|1
index|]
operator|=
name|hostport
expr_stmt|;
block|}
block|}
comment|// If command-line arguments, run our hack so they are executed.
comment|// ZOOKEEPER-1897 was committed to zookeeper-3.4.6 but elsewhere in this class we say
comment|// 3.4.6 breaks command-processing; TODO.
if|if
condition|(
name|hasCommandLineArguments
argument_list|(
name|args
argument_list|)
condition|)
block|{
name|HACK_UNTIL_ZOOKEEPER_1897_ZooKeeperMain
name|zkm
init|=
operator|new
name|HACK_UNTIL_ZOOKEEPER_1897_ZooKeeperMain
argument_list|(
name|newArgs
argument_list|)
decl_stmt|;
name|zkm
operator|.
name|runCmdLine
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|ZooKeeperMain
operator|.
name|main
argument_list|(
name|newArgs
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

