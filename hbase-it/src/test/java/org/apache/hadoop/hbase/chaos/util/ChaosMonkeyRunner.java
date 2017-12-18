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
operator|.
name|chaos
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
name|util
operator|.
name|Properties
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
name|lang3
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
name|IntegrationTestingUtility
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
name|chaos
operator|.
name|factories
operator|.
name|MonkeyFactory
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
name|ChaosMonkey
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
name|util
operator|.
name|ToolRunner
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
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Sets
import|;
end_import

begin_class
specifier|public
class|class
name|ChaosMonkeyRunner
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
name|ChaosMonkeyRunner
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|MONKEY_LONG_OPT
init|=
literal|"monkey"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|CHAOS_MONKEY_PROPS
init|=
literal|"monkeyProps"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|TABLE_NAME_OPT
init|=
literal|"tableName"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|FAMILY_NAME_OPT
init|=
literal|"familyName"
decl_stmt|;
specifier|protected
name|IntegrationTestingUtility
name|util
decl_stmt|;
specifier|protected
name|ChaosMonkey
name|monkey
decl_stmt|;
specifier|protected
name|String
name|monkeyToUse
decl_stmt|;
specifier|protected
name|Properties
name|monkeyProps
decl_stmt|;
specifier|protected
name|boolean
name|noClusterCleanUp
init|=
literal|false
decl_stmt|;
specifier|private
name|String
name|tableName
init|=
literal|"ChaosMonkeyRunner.tableName"
decl_stmt|;
specifier|private
name|String
name|familyName
init|=
literal|"ChaosMonkeyRunner.familyName"
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|addOptions
parameter_list|()
block|{
name|addOptWithArg
argument_list|(
literal|"m"
argument_list|,
name|MONKEY_LONG_OPT
argument_list|,
literal|"Which chaos monkey to run"
argument_list|)
expr_stmt|;
name|addOptWithArg
argument_list|(
name|CHAOS_MONKEY_PROPS
argument_list|,
literal|"The properties file for specifying chaos "
operator|+
literal|"monkey properties."
argument_list|)
expr_stmt|;
name|addOptWithArg
argument_list|(
name|TABLE_NAME_OPT
argument_list|,
literal|"Table name in the test to run chaos monkey against"
argument_list|)
expr_stmt|;
name|addOptWithArg
argument_list|(
name|FAMILY_NAME_OPT
argument_list|,
literal|"Family name in the test to run chaos monkey against"
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
if|if
condition|(
name|cmd
operator|.
name|hasOption
argument_list|(
name|MONKEY_LONG_OPT
argument_list|)
condition|)
block|{
name|monkeyToUse
operator|=
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|MONKEY_LONG_OPT
argument_list|)
expr_stmt|;
block|}
name|monkeyProps
operator|=
operator|new
name|Properties
argument_list|()
expr_stmt|;
if|if
condition|(
name|cmd
operator|.
name|hasOption
argument_list|(
name|CHAOS_MONKEY_PROPS
argument_list|)
condition|)
block|{
name|String
name|chaosMonkeyPropsFile
init|=
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|CHAOS_MONKEY_PROPS
argument_list|)
decl_stmt|;
if|if
condition|(
name|StringUtils
operator|.
name|isNotEmpty
argument_list|(
name|chaosMonkeyPropsFile
argument_list|)
condition|)
block|{
try|try
block|{
name|monkeyProps
operator|.
name|load
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getClassLoader
argument_list|()
operator|.
name|getResourceAsStream
argument_list|(
name|chaosMonkeyPropsFile
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
name|e
operator|.
name|toString
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
name|EXIT_FAILURE
argument_list|)
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|cmd
operator|.
name|hasOption
argument_list|(
name|TABLE_NAME_OPT
argument_list|)
condition|)
block|{
name|this
operator|.
name|tableName
operator|=
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|TABLE_NAME_OPT
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|cmd
operator|.
name|hasOption
argument_list|(
name|FAMILY_NAME_OPT
argument_list|)
condition|)
block|{
name|this
operator|.
name|familyName
operator|=
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|FAMILY_NAME_OPT
argument_list|)
expr_stmt|;
block|}
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
name|setUpCluster
argument_list|()
expr_stmt|;
name|getAndStartMonkey
argument_list|()
expr_stmt|;
while|while
condition|(
operator|!
name|monkey
operator|.
name|isStopped
argument_list|()
condition|)
block|{
comment|// loop here until got killed
try|try
block|{
comment|// TODO: make sleep time configurable
name|Thread
operator|.
name|sleep
argument_list|(
literal|5000
argument_list|)
expr_stmt|;
comment|// 5 seconds
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ite
parameter_list|)
block|{
comment|// Chaos monkeys got interrupted.
comment|// It is ok to stop monkeys and exit.
name|monkey
operator|.
name|stop
argument_list|(
literal|"Interruption occurred."
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
name|monkey
operator|.
name|waitForStop
argument_list|()
expr_stmt|;
return|return
literal|0
return|;
block|}
specifier|public
name|void
name|stopRunner
parameter_list|()
block|{
if|if
condition|(
name|monkey
operator|!=
literal|null
condition|)
block|{
name|monkey
operator|.
name|stop
argument_list|(
literal|"Program Control"
argument_list|)
expr_stmt|;
block|}
block|}
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
name|boolean
name|isDistributed
init|=
name|isDistributedCluster
argument_list|(
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|isDistributed
condition|)
block|{
name|util
operator|.
name|createDistributedHBaseCluster
argument_list|()
expr_stmt|;
name|util
operator|.
name|checkNodeCount
argument_list|(
literal|1
argument_list|)
expr_stmt|;
comment|// make sure there's at least 1 alive rs
block|}
else|else
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"ChaosMonkeyRunner must run againt a distributed cluster,"
operator|+
literal|" please check and point to the right configuration dir"
argument_list|)
throw|;
block|}
name|this
operator|.
name|setConf
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|boolean
name|isDistributedCluster
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getBoolean
argument_list|(
name|HConstants
operator|.
name|CLUSTER_DISTRIBUTED
argument_list|,
literal|false
argument_list|)
return|;
block|}
specifier|public
name|void
name|getAndStartMonkey
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
name|MonkeyFactory
name|fact
init|=
name|MonkeyFactory
operator|.
name|getFactory
argument_list|(
name|monkeyToUse
argument_list|)
decl_stmt|;
if|if
condition|(
name|fact
operator|==
literal|null
condition|)
block|{
name|fact
operator|=
name|getDefaultMonkeyFactory
argument_list|()
expr_stmt|;
block|}
name|monkey
operator|=
name|fact
operator|.
name|setUtil
argument_list|(
name|util
argument_list|)
operator|.
name|setTableName
argument_list|(
name|getTablename
argument_list|()
argument_list|)
operator|.
name|setProperties
argument_list|(
name|monkeyProps
argument_list|)
operator|.
name|setColumnFamilies
argument_list|(
name|getColumnFamilies
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|monkey
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
specifier|protected
name|IntegrationTestingUtility
name|getTestingUtil
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|util
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|conf
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|util
operator|=
operator|new
name|IntegrationTestingUtility
argument_list|()
expr_stmt|;
name|this
operator|.
name|setConf
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|util
operator|=
operator|new
name|IntegrationTestingUtility
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|util
return|;
block|}
specifier|protected
name|MonkeyFactory
name|getDefaultMonkeyFactory
parameter_list|()
block|{
comment|// Run with slow deterministic monkey by default
return|return
name|MonkeyFactory
operator|.
name|getFactory
argument_list|(
name|MonkeyFactory
operator|.
name|SLOW_DETERMINISTIC
argument_list|)
return|;
block|}
specifier|public
name|TableName
name|getTablename
parameter_list|()
block|{
return|return
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
return|;
block|}
specifier|protected
name|Set
argument_list|<
name|String
argument_list|>
name|getColumnFamilies
parameter_list|()
block|{
return|return
name|Sets
operator|.
name|newHashSet
argument_list|(
name|familyName
argument_list|)
return|;
block|}
comment|/*    * If caller wants to add config parameters contained in a file, the path of conf file    * can be passed as the first two arguments like this:    *   -c<path-to-conf>    */
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
name|String
index|[]
name|actualArgs
init|=
name|args
decl_stmt|;
if|if
condition|(
name|args
operator|.
name|length
operator|>
literal|0
operator|&&
literal|"-c"
operator|.
name|equals
argument_list|(
name|args
index|[
literal|0
index|]
argument_list|)
condition|)
block|{
name|int
name|argCount
init|=
name|args
operator|.
name|length
operator|-
literal|2
decl_stmt|;
if|if
condition|(
name|argCount
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Missing path for -c parameter"
argument_list|)
throw|;
block|}
comment|// load the resource specified by the second parameter
name|conf
operator|.
name|addResource
argument_list|(
name|args
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
name|actualArgs
operator|=
operator|new
name|String
index|[
name|argCount
index|]
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|args
argument_list|,
literal|2
argument_list|,
name|actualArgs
argument_list|,
literal|0
argument_list|,
name|argCount
argument_list|)
expr_stmt|;
block|}
name|IntegrationTestingUtility
operator|.
name|setUseDistributedCluster
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|int
name|ret
init|=
name|ToolRunner
operator|.
name|run
argument_list|(
name|conf
argument_list|,
operator|new
name|ChaosMonkeyRunner
argument_list|()
argument_list|,
name|actualArgs
argument_list|)
decl_stmt|;
name|System
operator|.
name|exit
argument_list|(
name|ret
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

