begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|edu
operator|.
name|umd
operator|.
name|cs
operator|.
name|findbugs
operator|.
name|annotations
operator|.
name|NonNull
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
name|lang
operator|.
name|reflect
operator|.
name|Method
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
name|Modifier
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
name|java
operator|.
name|util
operator|.
name|Set
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
name|hadoop
operator|.
name|hbase
operator|.
name|testclassification
operator|.
name|IntegrationTests
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
name|LargeTests
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
name|MediumTests
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
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|rules
operator|.
name|TestRule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|rules
operator|.
name|Timeout
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runner
operator|.
name|Description
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runner
operator|.
name|RunWith
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
operator|.
name|Parameters
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|model
operator|.
name|Statement
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
name|annotations
operator|.
name|VisibleForTesting
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
name|Iterables
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
name|Sets
import|;
end_import

begin_comment
comment|/**  * The class level TestRule for all the tests. Every test class should have a {@code ClassRule} with  * it.  *<p>  * For now it only sets a test method timeout based off the test categories small, medium, large.  * Based on junit Timeout TestRule; see https://github.com/junit-team/junit/wiki/Rules  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|HBaseClassTestRule
implements|implements
name|TestRule
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
name|HBaseClassTestRule
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Set
argument_list|<
name|Class
argument_list|<
name|?
argument_list|>
argument_list|>
name|UNIT_TEST_CLASSES
init|=
name|Collections
operator|.
name|unmodifiableSet
argument_list|(
name|Sets
operator|.
expr|<
name|Class
argument_list|<
name|?
argument_list|>
operator|>
name|newHashSet
argument_list|(
name|SmallTests
operator|.
name|class
argument_list|,
name|MediumTests
operator|.
name|class
argument_list|,
name|LargeTests
operator|.
name|class
argument_list|)
argument_list|)
decl_stmt|;
comment|// Each unit test has this timeout.
specifier|private
specifier|static
name|long
name|PER_UNIT_TEST_TIMEOUT_MINS
init|=
literal|13
decl_stmt|;
specifier|private
specifier|final
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
decl_stmt|;
specifier|private
specifier|final
name|Timeout
name|timeout
decl_stmt|;
specifier|private
name|HBaseClassTestRule
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
parameter_list|,
name|Timeout
name|timeout
parameter_list|)
block|{
name|this
operator|.
name|clazz
operator|=
name|clazz
expr_stmt|;
name|this
operator|.
name|timeout
operator|=
name|timeout
expr_stmt|;
block|}
comment|/**    * Mainly used for {@link HBaseClassTestRuleChecker} to confirm that we use the correct    * class to generate timeout ClassRule.    */
specifier|public
name|Class
argument_list|<
name|?
argument_list|>
name|getClazz
parameter_list|()
block|{
return|return
name|clazz
return|;
block|}
specifier|private
specifier|static
name|long
name|getTimeoutInSeconds
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
parameter_list|)
block|{
name|Category
index|[]
name|categories
init|=
name|clazz
operator|.
name|getAnnotationsByType
argument_list|(
name|Category
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// Starting JUnit 4.13, it appears that the timeout is applied across all the parameterized
comment|// runs. So the timeout is multiplied by number of parameterized runs.
name|int
name|numParams
init|=
name|getNumParameters
argument_list|(
name|clazz
argument_list|)
decl_stmt|;
comment|// @Category is not repeatable -- it is only possible to get an array of length zero or one.
if|if
condition|(
name|categories
operator|.
name|length
operator|==
literal|1
condition|)
block|{
for|for
control|(
name|Class
argument_list|<
name|?
argument_list|>
name|c
range|:
name|categories
index|[
literal|0
index|]
operator|.
name|value
argument_list|()
control|)
block|{
if|if
condition|(
name|UNIT_TEST_CLASSES
operator|.
name|contains
argument_list|(
name|c
argument_list|)
condition|)
block|{
name|long
name|timeout
init|=
name|numParams
operator|*
name|PER_UNIT_TEST_TIMEOUT_MINS
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Test {} timeout: {} mins"
argument_list|,
name|clazz
argument_list|,
name|timeout
argument_list|)
expr_stmt|;
return|return
name|TimeUnit
operator|.
name|MINUTES
operator|.
name|toSeconds
argument_list|(
name|timeout
argument_list|)
return|;
block|}
if|if
condition|(
name|c
operator|==
name|IntegrationTests
operator|.
name|class
condition|)
block|{
return|return
name|TimeUnit
operator|.
name|MINUTES
operator|.
name|toSeconds
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|)
return|;
block|}
block|}
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|clazz
operator|.
name|getName
argument_list|()
operator|+
literal|" does not have SmallTests/MediumTests/LargeTests in @Category"
argument_list|)
throw|;
block|}
comment|/**    * @param clazz Test class that is running.    * @return the number of parameters for this given test class. If the test is not parameterized or    *   if there is any issue determining the number of parameters, returns 1.    */
annotation|@
name|VisibleForTesting
specifier|static
name|int
name|getNumParameters
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
parameter_list|)
block|{
name|RunWith
index|[]
name|runWiths
init|=
name|clazz
operator|.
name|getAnnotationsByType
argument_list|(
name|RunWith
operator|.
name|class
argument_list|)
decl_stmt|;
name|boolean
name|testParameterized
init|=
name|runWiths
operator|!=
literal|null
operator|&&
name|Arrays
operator|.
name|stream
argument_list|(
name|runWiths
argument_list|)
operator|.
name|anyMatch
argument_list|(
parameter_list|(
name|r
parameter_list|)
lambda|->
name|r
operator|.
name|value
argument_list|()
operator|.
name|equals
argument_list|(
name|Parameterized
operator|.
name|class
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|testParameterized
condition|)
block|{
return|return
literal|1
return|;
block|}
for|for
control|(
name|Method
name|method
range|:
name|clazz
operator|.
name|getMethods
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|isParametersMethod
argument_list|(
name|method
argument_list|)
condition|)
block|{
continue|continue;
block|}
comment|// Found the parameters method. Figure out the number of parameters.
name|Object
name|parameters
decl_stmt|;
try|try
block|{
name|parameters
operator|=
name|method
operator|.
name|invoke
argument_list|(
name|clazz
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalAccessException
decl||
name|InvocationTargetException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Error invoking parameters method {} in test class {}"
argument_list|,
name|method
operator|.
name|getName
argument_list|()
argument_list|,
name|clazz
argument_list|,
name|e
argument_list|)
expr_stmt|;
continue|continue;
block|}
if|if
condition|(
name|parameters
operator|instanceof
name|List
condition|)
block|{
return|return
operator|(
operator|(
name|List
operator|)
name|parameters
operator|)
operator|.
name|size
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|parameters
operator|instanceof
name|Collection
condition|)
block|{
return|return
operator|(
operator|(
name|Collection
operator|)
name|parameters
operator|)
operator|.
name|size
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|parameters
operator|instanceof
name|Iterable
condition|)
block|{
return|return
name|Iterables
operator|.
name|size
argument_list|(
operator|(
name|Iterable
operator|)
name|parameters
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|parameters
operator|instanceof
name|Object
index|[]
condition|)
block|{
return|return
operator|(
operator|(
name|Object
index|[]
operator|)
name|parameters
operator|)
operator|.
name|length
return|;
block|}
block|}
name|LOG
operator|.
name|warn
argument_list|(
literal|"Unable to determine parameters size. Returning the default of 1."
argument_list|)
expr_stmt|;
return|return
literal|1
return|;
block|}
comment|/**    * Helper method that checks if the input method is a valid JUnit @Parameters method.    * @param method Input method.    * @return true if the method is a valid JUnit parameters method, false otherwise.    */
specifier|private
specifier|static
name|boolean
name|isParametersMethod
parameter_list|(
annotation|@
name|NonNull
name|Method
name|method
parameter_list|)
block|{
comment|// A valid parameters method is public static and with @Parameters annotation.
name|boolean
name|methodPublicStatic
init|=
name|Modifier
operator|.
name|isPublic
argument_list|(
name|method
operator|.
name|getModifiers
argument_list|()
argument_list|)
operator|&&
name|Modifier
operator|.
name|isStatic
argument_list|(
name|method
operator|.
name|getModifiers
argument_list|()
argument_list|)
decl_stmt|;
name|Parameters
index|[]
name|params
init|=
name|method
operator|.
name|getAnnotationsByType
argument_list|(
name|Parameters
operator|.
name|class
argument_list|)
decl_stmt|;
return|return
name|methodPublicStatic
operator|&&
operator|(
name|params
operator|!=
literal|null
operator|&&
name|params
operator|.
name|length
operator|>
literal|0
operator|)
return|;
block|}
specifier|public
specifier|static
name|HBaseClassTestRule
name|forClass
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
parameter_list|)
block|{
return|return
operator|new
name|HBaseClassTestRule
argument_list|(
name|clazz
argument_list|,
name|Timeout
operator|.
name|builder
argument_list|()
operator|.
name|withLookingForStuckThread
argument_list|(
literal|true
argument_list|)
operator|.
name|withTimeout
argument_list|(
name|getTimeoutInSeconds
argument_list|(
name|clazz
argument_list|)
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Statement
name|apply
parameter_list|(
name|Statement
name|base
parameter_list|,
name|Description
name|description
parameter_list|)
block|{
return|return
name|timeout
operator|.
name|apply
argument_list|(
name|base
argument_list|,
name|description
argument_list|)
return|;
block|}
block|}
end_class

end_unit

