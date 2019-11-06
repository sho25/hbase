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
name|factories
package|;
end_package

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
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableMap
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
name|ReflectionUtils
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

begin_comment
comment|/**  * Base class of the factory that will create a ChaosMonkey.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|MonkeyFactory
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
name|MonkeyFactory
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
name|TableName
name|tableName
decl_stmt|;
specifier|protected
name|Set
argument_list|<
name|String
argument_list|>
name|columnFamilies
decl_stmt|;
specifier|protected
name|IntegrationTestingUtility
name|util
decl_stmt|;
specifier|protected
name|Properties
name|properties
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
specifier|public
name|MonkeyFactory
name|setTableName
parameter_list|(
name|TableName
name|tableName
parameter_list|)
block|{
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|MonkeyFactory
name|setColumnFamilies
parameter_list|(
name|Set
argument_list|<
name|String
argument_list|>
name|columnFamilies
parameter_list|)
block|{
name|this
operator|.
name|columnFamilies
operator|=
name|columnFamilies
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|MonkeyFactory
name|setUtil
parameter_list|(
name|IntegrationTestingUtility
name|util
parameter_list|)
block|{
name|this
operator|.
name|util
operator|=
name|util
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|MonkeyFactory
name|setProperties
parameter_list|(
name|Properties
name|props
parameter_list|)
block|{
if|if
condition|(
name|props
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|properties
operator|=
name|props
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
specifier|public
specifier|abstract
name|ChaosMonkey
name|build
parameter_list|()
function_decl|;
specifier|public
specifier|static
specifier|final
name|String
name|CALM
init|=
literal|"calm"
decl_stmt|;
comment|// TODO: the name has become a misnomer since the default (not-slow) monkey has been removed
specifier|public
specifier|static
specifier|final
name|String
name|SLOW_DETERMINISTIC
init|=
literal|"slowDeterministic"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|UNBALANCE
init|=
literal|"unbalance"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SERVER_KILLING
init|=
literal|"serverKilling"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|STRESS_AM
init|=
literal|"stressAM"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|NO_KILL
init|=
literal|"noKill"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|MASTER_KILLING
init|=
literal|"masterKilling"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|MOB_NO_KILL
init|=
literal|"mobNoKill"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|MOB_SLOW_DETERMINISTIC
init|=
literal|"mobSlowDeterministic"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SERVER_AND_DEPENDENCIES_KILLING
init|=
literal|"serverAndDependenciesKilling"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DISTRIBUTED_ISSUES
init|=
literal|"distributedIssues"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DATA_ISSUES
init|=
literal|"dataIssues"
decl_stmt|;
specifier|public
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|MonkeyFactory
argument_list|>
name|FACTORIES
init|=
name|ImmutableMap
operator|.
expr|<
name|String
decl_stmt|,
name|MonkeyFactory
decl|>
name|builder
argument_list|()
decl|.
name|put
argument_list|(
name|CALM
argument_list|,
operator|new
name|CalmMonkeyFactory
argument_list|()
argument_list|)
decl|.
name|put
argument_list|(
name|SLOW_DETERMINISTIC
argument_list|,
operator|new
name|SlowDeterministicMonkeyFactory
argument_list|()
argument_list|)
decl|.
name|put
argument_list|(
name|UNBALANCE
argument_list|,
operator|new
name|UnbalanceMonkeyFactory
argument_list|()
argument_list|)
decl|.
name|put
argument_list|(
name|SERVER_KILLING
argument_list|,
operator|new
name|ServerKillingMonkeyFactory
argument_list|()
argument_list|)
decl|.
name|put
argument_list|(
name|STRESS_AM
argument_list|,
operator|new
name|StressAssignmentManagerMonkeyFactory
argument_list|()
argument_list|)
decl|.
name|put
argument_list|(
name|NO_KILL
argument_list|,
operator|new
name|NoKillMonkeyFactory
argument_list|()
argument_list|)
decl|.
name|put
argument_list|(
name|MASTER_KILLING
argument_list|,
operator|new
name|MasterKillingMonkeyFactory
argument_list|()
argument_list|)
decl|.
name|put
argument_list|(
name|MOB_NO_KILL
argument_list|,
operator|new
name|MobNoKillMonkeyFactory
argument_list|()
argument_list|)
decl|.
name|put
argument_list|(
name|MOB_SLOW_DETERMINISTIC
argument_list|,
operator|new
name|MobNoKillMonkeyFactory
argument_list|()
argument_list|)
decl|.
name|put
argument_list|(
name|SERVER_AND_DEPENDENCIES_KILLING
argument_list|,
operator|new
name|ServerAndDependenciesKillingMonkeyFactory
argument_list|()
argument_list|)
decl|.
name|put
argument_list|(
name|DISTRIBUTED_ISSUES
argument_list|,
operator|new
name|DistributedIssuesMonkeyFactory
argument_list|()
argument_list|)
decl|.
name|put
argument_list|(
name|DATA_ISSUES
argument_list|,
operator|new
name|DataIssuesMonkeyFactory
argument_list|()
argument_list|)
decl|.
name|build
argument_list|()
decl_stmt|;
specifier|public
specifier|static
name|MonkeyFactory
name|getFactory
parameter_list|(
name|String
name|factoryName
parameter_list|)
block|{
name|MonkeyFactory
name|fact
init|=
name|FACTORIES
operator|.
name|get
argument_list|(
name|factoryName
argument_list|)
decl_stmt|;
if|if
condition|(
name|fact
operator|==
literal|null
operator|&&
name|factoryName
operator|!=
literal|null
operator|&&
operator|!
name|factoryName
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|Class
name|klass
init|=
literal|null
decl_stmt|;
try|try
block|{
name|klass
operator|=
name|Class
operator|.
name|forName
argument_list|(
name|factoryName
argument_list|)
expr_stmt|;
if|if
condition|(
name|klass
operator|!=
literal|null
condition|)
block|{
name|fact
operator|=
operator|(
name|MonkeyFactory
operator|)
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|klass
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error trying to create "
operator|+
name|factoryName
operator|+
literal|" could not load it by class name"
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
return|return
name|fact
return|;
block|}
block|}
end_class

end_unit

