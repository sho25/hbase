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
name|constraint
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
name|assertFalse
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
name|assertTrue
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
name|HBaseClassTestRule
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
name|HTableDescriptor
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
name|client
operator|.
name|Put
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
name|constraint
operator|.
name|TestConstraint
operator|.
name|CheckWasRunConstraint
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
name|constraint
operator|.
name|WorksConstraint
operator|.
name|NameConstraint
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
name|MiscTests
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
name|Rule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
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
name|TestName
import|;
end_import

begin_comment
comment|/**  * Test reading/writing the constraints into the {@link HTableDescriptor}  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MiscTests
operator|.
name|class
block|,
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestConstraints
block|{
annotation|@
name|ClassRule
specifier|public
specifier|static
specifier|final
name|HBaseClassTestRule
name|CLASS_RULE
init|=
name|HBaseClassTestRule
operator|.
name|forClass
argument_list|(
name|TestConstraints
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|TestName
name|name
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
annotation|@
name|Test
specifier|public
name|void
name|testSimpleReadWrite
parameter_list|()
throws|throws
name|Throwable
block|{
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|Constraints
operator|.
name|add
argument_list|(
name|desc
argument_list|,
name|WorksConstraint
operator|.
name|class
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|?
extends|extends
name|Constraint
argument_list|>
name|constraints
init|=
name|Constraints
operator|.
name|getConstraints
argument_list|(
name|desc
argument_list|,
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getClassLoader
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|constraints
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|WorksConstraint
operator|.
name|class
argument_list|,
name|constraints
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
comment|// Check that we can add more than 1 constraint and that ordering is
comment|// preserved
name|Constraints
operator|.
name|add
argument_list|(
name|desc
argument_list|,
name|AlsoWorks
operator|.
name|class
argument_list|,
name|NameConstraint
operator|.
name|class
argument_list|)
expr_stmt|;
name|constraints
operator|=
name|Constraints
operator|.
name|getConstraints
argument_list|(
name|desc
argument_list|,
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getClassLoader
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|constraints
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|WorksConstraint
operator|.
name|class
argument_list|,
name|constraints
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|AlsoWorks
operator|.
name|class
argument_list|,
name|constraints
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|NameConstraint
operator|.
name|class
argument_list|,
name|constraints
operator|.
name|get
argument_list|(
literal|2
argument_list|)
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
annotation|@
name|Test
specifier|public
name|void
name|testReadWriteWithConf
parameter_list|()
throws|throws
name|Throwable
block|{
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|Constraints
operator|.
name|add
argument_list|(
name|desc
argument_list|,
operator|new
name|Pair
argument_list|<>
argument_list|(
name|CheckConfigurationConstraint
operator|.
name|class
argument_list|,
name|CheckConfigurationConstraint
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|?
extends|extends
name|Constraint
argument_list|>
name|c
init|=
name|Constraints
operator|.
name|getConstraints
argument_list|(
name|desc
argument_list|,
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getClassLoader
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|c
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|CheckConfigurationConstraint
operator|.
name|class
argument_list|,
name|c
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
comment|// check to make sure that we overwrite configurations
name|Constraints
operator|.
name|add
argument_list|(
name|desc
argument_list|,
operator|new
name|Pair
argument_list|<>
argument_list|(
name|CheckConfigurationConstraint
operator|.
name|class
argument_list|,
operator|new
name|Configuration
argument_list|(
literal|false
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|Constraints
operator|.
name|getConstraints
argument_list|(
name|desc
argument_list|,
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getClassLoader
argument_list|()
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"No exception thrown  - configuration not overwritten"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
comment|// expect to have the exception, so don't do anything
block|}
block|}
comment|/**    * Test that Constraints are properly enabled, disabled, and removed    *    * @throws Exception    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
annotation|@
name|Test
specifier|public
name|void
name|testEnableDisableRemove
parameter_list|()
throws|throws
name|Exception
block|{
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
comment|// check general enabling/disabling of constraints
comment|// first add a constraint
name|Constraints
operator|.
name|add
argument_list|(
name|desc
argument_list|,
name|AllPassConstraint
operator|.
name|class
argument_list|)
expr_stmt|;
comment|// make sure everything is enabled
name|assertTrue
argument_list|(
name|Constraints
operator|.
name|enabled
argument_list|(
name|desc
argument_list|,
name|AllPassConstraint
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|desc
operator|.
name|hasCoprocessor
argument_list|(
name|ConstraintProcessor
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// check disabling
name|Constraints
operator|.
name|disable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|desc
operator|.
name|hasCoprocessor
argument_list|(
name|ConstraintProcessor
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// make sure the added constraints are still present
name|assertTrue
argument_list|(
name|Constraints
operator|.
name|enabled
argument_list|(
name|desc
argument_list|,
name|AllPassConstraint
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
comment|// check just removing the single constraint
name|Constraints
operator|.
name|remove
argument_list|(
name|desc
argument_list|,
name|AllPassConstraint
operator|.
name|class
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|Constraints
operator|.
name|has
argument_list|(
name|desc
argument_list|,
name|AllPassConstraint
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
comment|// Add back the single constraint
name|Constraints
operator|.
name|add
argument_list|(
name|desc
argument_list|,
name|AllPassConstraint
operator|.
name|class
argument_list|)
expr_stmt|;
comment|// and now check that when we remove constraints, all are gone
name|Constraints
operator|.
name|remove
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|desc
operator|.
name|hasCoprocessor
argument_list|(
name|ConstraintProcessor
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|Constraints
operator|.
name|has
argument_list|(
name|desc
argument_list|,
name|AllPassConstraint
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test that when we update a constraint the ordering is not modified.    *    * @throws Exception    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
annotation|@
name|Test
specifier|public
name|void
name|testUpdateConstraint
parameter_list|()
throws|throws
name|Exception
block|{
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|Constraints
operator|.
name|add
argument_list|(
name|desc
argument_list|,
name|CheckConfigurationConstraint
operator|.
name|class
argument_list|,
name|CheckWasRunConstraint
operator|.
name|class
argument_list|)
expr_stmt|;
name|Constraints
operator|.
name|setConfiguration
argument_list|(
name|desc
argument_list|,
name|CheckConfigurationConstraint
operator|.
name|class
argument_list|,
name|CheckConfigurationConstraint
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|?
extends|extends
name|Constraint
argument_list|>
name|constraints
init|=
name|Constraints
operator|.
name|getConstraints
argument_list|(
name|desc
argument_list|,
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getClassLoader
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|constraints
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// check to make sure the order didn't change
name|assertEquals
argument_list|(
name|CheckConfigurationConstraint
operator|.
name|class
argument_list|,
name|constraints
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|CheckWasRunConstraint
operator|.
name|class
argument_list|,
name|constraints
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test that if a constraint hasn't been set that there are no problems with    * attempting to remove it.    *    * @throws Throwable    *           on failure.    */
annotation|@
name|Test
specifier|public
name|void
name|testRemoveUnsetConstraint
parameter_list|()
throws|throws
name|Throwable
block|{
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|Constraints
operator|.
name|remove
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|Constraints
operator|.
name|remove
argument_list|(
name|desc
argument_list|,
name|AlsoWorks
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testConfigurationPreserved
parameter_list|()
throws|throws
name|Throwable
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
literal|"_ENABLED"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
literal|"_PRIORITY"
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|Constraints
operator|.
name|add
argument_list|(
name|desc
argument_list|,
name|AlsoWorks
operator|.
name|class
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|Constraints
operator|.
name|add
argument_list|(
name|desc
argument_list|,
name|WorksConstraint
operator|.
name|class
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|Constraints
operator|.
name|enabled
argument_list|(
name|desc
argument_list|,
name|AlsoWorks
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|?
extends|extends
name|Constraint
argument_list|>
name|constraints
init|=
name|Constraints
operator|.
name|getConstraints
argument_list|(
name|desc
argument_list|,
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getClassLoader
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Constraint
name|c
range|:
name|constraints
control|)
block|{
name|Configuration
name|storedConf
init|=
name|c
operator|.
name|getConf
argument_list|()
decl_stmt|;
if|if
condition|(
name|c
operator|instanceof
name|AlsoWorks
condition|)
name|assertEquals
argument_list|(
literal|10
argument_list|,
name|storedConf
operator|.
name|getLong
argument_list|(
literal|"_PRIORITY"
argument_list|,
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
comment|// its just a worksconstraint
else|else
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|storedConf
operator|.
name|getLong
argument_list|(
literal|"_PRIORITY"
argument_list|,
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|// ---------- Constraints just used for testing
comment|/**    * Also just works    */
specifier|public
specifier|static
class|class
name|AlsoWorks
extends|extends
name|BaseConstraint
block|{
annotation|@
name|Override
specifier|public
name|void
name|check
parameter_list|(
name|Put
name|p
parameter_list|)
block|{
comment|// NOOP
block|}
block|}
block|}
end_class

end_unit

