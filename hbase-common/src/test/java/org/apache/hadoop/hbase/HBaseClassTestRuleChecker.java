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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

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
name|lang
operator|.
name|reflect
operator|.
name|Field
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
name|ClassRule
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
name|notification
operator|.
name|RunListener
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
name|notification
operator|.
name|RunListener
operator|.
name|ThreadSafe
import|;
end_import

begin_comment
comment|/**  * A RunListener to confirm that we have a {@link CategoryBasedTimeout} class rule for every test.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|ThreadSafe
specifier|public
class|class
name|HBaseClassTestRuleChecker
extends|extends
name|RunListener
block|{
annotation|@
name|Override
specifier|public
name|void
name|testStarted
parameter_list|(
name|Description
name|description
parameter_list|)
throws|throws
name|Exception
block|{
for|for
control|(
name|Field
name|field
range|:
name|description
operator|.
name|getTestClass
argument_list|()
operator|.
name|getFields
argument_list|()
control|)
block|{
if|if
condition|(
name|Modifier
operator|.
name|isStatic
argument_list|(
name|field
operator|.
name|getModifiers
argument_list|()
argument_list|)
operator|&&
name|field
operator|.
name|getType
argument_list|()
operator|==
name|HBaseClassTestRule
operator|.
name|class
operator|&&
name|field
operator|.
name|isAnnotationPresent
argument_list|(
name|ClassRule
operator|.
name|class
argument_list|)
condition|)
block|{
name|HBaseClassTestRule
name|timeout
init|=
operator|(
name|HBaseClassTestRule
operator|)
name|field
operator|.
name|get
argument_list|(
literal|null
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"The HBaseClassTestRule ClassRule in "
operator|+
name|description
operator|.
name|getTestClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|" is for "
operator|+
name|timeout
operator|.
name|getClazz
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|description
operator|.
name|getTestClass
argument_list|()
argument_list|,
name|timeout
operator|.
name|getClazz
argument_list|()
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
name|fail
argument_list|(
literal|"No HBaseClassTestRule ClassRule for "
operator|+
name|description
operator|.
name|getTestClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

