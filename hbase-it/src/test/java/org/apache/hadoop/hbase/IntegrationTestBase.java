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

begin_comment
comment|/**  * Base class for HBase integration tests that want to use the Chaos Monkey.  * Usage: bin/hbase<sub_class_of_IntegrationTestBase><options>  * Options: -h,--help Show usage  *          -m,--monkey<arg> Which chaos monkey to run  *          -monkeyProps<arg> The properties file for specifying chaos monkey properties.  *          -ncc Option to not clean up the cluster at the end.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|IntegrationTestBase
extends|extends
name|AbstractHBaseTool
block|{
specifier|public
specifier|static
specifier|final
name|String
name|NO_CLUSTER_CLEANUP_LONG_OPT
init|=
literal|"noClusterCleanUp"
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
name|IntegrationTestBase
operator|.
name|class
argument_list|)
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
specifier|public
name|IntegrationTestBase
parameter_list|()
block|{
name|this
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|IntegrationTestBase
parameter_list|(
name|String
name|monkeyToUse
parameter_list|)
block|{
name|this
operator|.
name|monkeyToUse
operator|=
name|monkeyToUse
expr_stmt|;
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
literal|"m"
argument_list|,
name|MONKEY_LONG_OPT
argument_list|,
literal|"Which chaos monkey to run"
argument_list|)
expr_stmt|;
name|addOptNoArg
argument_list|(
literal|"ncc"
argument_list|,
name|NO_CLUSTER_CLEANUP_LONG_OPT
argument_list|,
literal|"Don't clean up the cluster at the end"
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
block|}
comment|/**    * This allows tests that subclass children of this base class such as    * {@link org.apache.hadoop.hbase.test.IntegrationTestReplication} to    * include the base options without having to also include the options from the test.    *    * @param cmd the command line    */
specifier|protected
name|void
name|processBaseOptions
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
if|if
condition|(
name|cmd
operator|.
name|hasOption
argument_list|(
name|NO_CLUSTER_CLEANUP_LONG_OPT
argument_list|)
condition|)
block|{
name|noClusterCleanUp
operator|=
literal|true
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
name|processBaseOptions
argument_list|(
name|cmd
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Configuration
name|getConf
parameter_list|()
block|{
name|Configuration
name|c
init|=
name|super
operator|.
name|getConf
argument_list|()
decl_stmt|;
if|if
condition|(
name|c
operator|==
literal|null
operator|&&
name|util
operator|!=
literal|null
condition|)
block|{
name|conf
operator|=
name|util
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|c
operator|=
name|conf
expr_stmt|;
block|}
return|return
name|c
return|;
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
name|setUp
argument_list|()
expr_stmt|;
name|int
name|result
init|=
operator|-
literal|1
decl_stmt|;
try|try
block|{
name|result
operator|=
name|runTestFromCommandLine
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|cleanUp
argument_list|()
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|setUpCluster
argument_list|()
expr_stmt|;
name|setUpMonkey
argument_list|()
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|cleanUp
parameter_list|()
throws|throws
name|Exception
block|{
name|cleanUpMonkey
argument_list|()
expr_stmt|;
name|cleanUpCluster
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|setUpMonkey
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
name|startMonkey
argument_list|()
expr_stmt|;
block|}
specifier|protected
name|MonkeyFactory
name|getDefaultMonkeyFactory
parameter_list|()
block|{
comment|// Run with no monkey in distributed context, with real monkey in local test context.
return|return
name|MonkeyFactory
operator|.
name|getFactory
argument_list|(
name|util
operator|.
name|isDistributedCluster
argument_list|()
condition|?
name|MonkeyFactory
operator|.
name|CALM
else|:
name|MonkeyFactory
operator|.
name|SLOW_DETERMINISTIC
argument_list|)
return|;
block|}
specifier|protected
name|void
name|startMonkey
parameter_list|()
throws|throws
name|Exception
block|{
name|monkey
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|cleanUpMonkey
parameter_list|()
throws|throws
name|Exception
block|{
name|cleanUpMonkey
argument_list|(
literal|"Ending test"
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|cleanUpMonkey
parameter_list|(
name|String
name|why
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|monkey
operator|!=
literal|null
operator|&&
operator|!
name|monkey
operator|.
name|isStopped
argument_list|()
condition|)
block|{
name|monkey
operator|.
name|stop
argument_list|(
name|why
argument_list|)
expr_stmt|;
name|monkey
operator|.
name|waitForStop
argument_list|()
expr_stmt|;
block|}
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
specifier|public
specifier|abstract
name|void
name|setUpCluster
parameter_list|()
throws|throws
name|Exception
function_decl|;
specifier|public
name|void
name|cleanUpCluster
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|util
operator|.
name|isDistributedCluster
argument_list|()
operator|&&
operator|(
name|monkey
operator|==
literal|null
operator|||
operator|!
name|monkey
operator|.
name|isDestructive
argument_list|()
operator|)
condition|)
block|{
name|noClusterCleanUp
operator|=
literal|true
expr_stmt|;
block|}
if|if
condition|(
name|noClusterCleanUp
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"noClusterCleanUp is set, skip restoring the cluster"
argument_list|)
expr_stmt|;
return|return;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Restoring the cluster"
argument_list|)
expr_stmt|;
name|util
operator|.
name|restoreCluster
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Done restoring the cluster"
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|abstract
name|int
name|runTestFromCommandLine
parameter_list|()
throws|throws
name|Exception
function_decl|;
comment|/**    * Provides the name of the table that is protected from random Chaos monkey activity    * @return table to not delete.    */
specifier|public
specifier|abstract
name|TableName
name|getTablename
parameter_list|()
function_decl|;
comment|/**    * Provides the name of the CFs that are protected from random Chaos monkey activity (alter)    * @return set of cf names to protect.    */
specifier|protected
specifier|abstract
name|Set
argument_list|<
name|String
argument_list|>
name|getColumnFamilies
parameter_list|()
function_decl|;
block|}
end_class

end_unit

