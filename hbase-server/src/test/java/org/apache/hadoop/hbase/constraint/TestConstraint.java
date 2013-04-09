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
name|hbase
operator|.
name|HBaseTestingUtility
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
name|HColumnDescriptor
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
name|client
operator|.
name|HTable
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
name|client
operator|.
name|RetriesExhaustedWithDetailsException
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
name|exceptions
operator|.
name|ConstraintException
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
name|Bytes
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
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
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

begin_comment
comment|/**  * Do the complex testing of constraints against a minicluster  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|MediumTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestConstraint
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
name|TestConstraint
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|HBaseTestingUtility
name|util
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|tableName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|dummy
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"dummy"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|row1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"r1"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|test
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|util
operator|=
operator|new
name|HBaseTestingUtility
argument_list|()
expr_stmt|;
name|util
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
block|}
comment|/**    * Test that we run a passing constraint    * @throws Exception    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
annotation|@
name|Test
specifier|public
name|void
name|testConstraintPasses
parameter_list|()
throws|throws
name|Exception
block|{
comment|// create the table
comment|// it would be nice if this was also a method on the util
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|family
range|:
operator|new
name|byte
index|[]
index|[]
block|{
name|dummy
block|,
name|test
block|}
control|)
block|{
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|family
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// add a constraint
name|Constraints
operator|.
name|add
argument_list|(
name|desc
argument_list|,
name|CheckWasRunConstraint
operator|.
name|class
argument_list|)
expr_stmt|;
name|util
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|table
operator|.
name|setAutoFlush
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// test that we don't fail on a valid put
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|row1
argument_list|)
decl_stmt|;
name|byte
index|[]
name|value
init|=
name|Integer
operator|.
name|toString
argument_list|(
literal|10
argument_list|)
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|dummy
argument_list|,
operator|new
name|byte
index|[
literal|0
index|]
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|CheckWasRunConstraint
operator|.
name|wasRun
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test that constraints will fail properly    * @throws Exception    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testConstraintFails
parameter_list|()
throws|throws
name|Exception
block|{
comment|// create the table
comment|// it would be nice if this was also a method on the util
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|family
range|:
operator|new
name|byte
index|[]
index|[]
block|{
name|dummy
block|,
name|test
block|}
control|)
block|{
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|family
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// add a constraint that is sure to fail
name|Constraints
operator|.
name|add
argument_list|(
name|desc
argument_list|,
name|AllFailConstraint
operator|.
name|class
argument_list|)
expr_stmt|;
name|util
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|table
operator|.
name|setAutoFlush
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// test that we do fail on violation
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|row1
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|dummy
argument_list|,
operator|new
name|byte
index|[
literal|0
index|]
argument_list|,
literal|"fail"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"Doing put in table"
argument_list|)
expr_stmt|;
try|try
block|{
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"This put should not have suceeded - AllFailConstraint was not run!"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RetriesExhaustedWithDetailsException
name|e
parameter_list|)
block|{
name|List
argument_list|<
name|Throwable
argument_list|>
name|causes
init|=
name|e
operator|.
name|getCauses
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"More than one failure cause - should only be the failure constraint exception"
argument_list|,
literal|1
argument_list|,
name|causes
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Throwable
name|t
init|=
name|causes
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|ConstraintException
operator|.
name|class
argument_list|,
name|t
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/**    * Check that if we just disable one constraint, then    * @throws Throwable    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
annotation|@
name|Test
specifier|public
name|void
name|testDisableConstraint
parameter_list|()
throws|throws
name|Throwable
block|{
comment|// create the table
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
comment|// add a family to the table
for|for
control|(
name|byte
index|[]
name|family
range|:
operator|new
name|byte
index|[]
index|[]
block|{
name|dummy
block|,
name|test
block|}
control|)
block|{
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|family
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// add a constraint to make sure it others get run
name|Constraints
operator|.
name|add
argument_list|(
name|desc
argument_list|,
name|CheckWasRunConstraint
operator|.
name|class
argument_list|)
expr_stmt|;
comment|// Add Constraint to check
name|Constraints
operator|.
name|add
argument_list|(
name|desc
argument_list|,
name|AllFailConstraint
operator|.
name|class
argument_list|)
expr_stmt|;
comment|// and then disable the failing constraint
name|Constraints
operator|.
name|disableConstraint
argument_list|(
name|desc
argument_list|,
name|AllFailConstraint
operator|.
name|class
argument_list|)
expr_stmt|;
name|util
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|table
operator|.
name|setAutoFlush
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// test that we don't fail because its disabled
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|row1
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|dummy
argument_list|,
operator|new
name|byte
index|[
literal|0
index|]
argument_list|,
literal|"pass"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|CheckWasRunConstraint
operator|.
name|wasRun
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test that if we disable all constraints, then nothing gets run    * @throws Throwable    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
annotation|@
name|Test
specifier|public
name|void
name|testDisableConstraints
parameter_list|()
throws|throws
name|Throwable
block|{
comment|// create the table
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
comment|// add a family to the table
for|for
control|(
name|byte
index|[]
name|family
range|:
operator|new
name|byte
index|[]
index|[]
block|{
name|dummy
block|,
name|test
block|}
control|)
block|{
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|family
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// add a constraint to check to see if is run
name|Constraints
operator|.
name|add
argument_list|(
name|desc
argument_list|,
name|CheckWasRunConstraint
operator|.
name|class
argument_list|)
expr_stmt|;
comment|// then disable all the constraints
name|Constraints
operator|.
name|disable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|util
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|table
operator|.
name|setAutoFlush
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// test that we do fail on violation
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|row1
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|dummy
argument_list|,
operator|new
name|byte
index|[
literal|0
index|]
argument_list|,
literal|"pass"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"Doing put in table"
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|CheckWasRunConstraint
operator|.
name|wasRun
argument_list|)
expr_stmt|;
block|}
comment|/**    * Check to make sure a constraint is unloaded when it fails    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testIsUnloaded
parameter_list|()
throws|throws
name|Exception
block|{
comment|// create the table
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
comment|// add a family to the table
for|for
control|(
name|byte
index|[]
name|family
range|:
operator|new
name|byte
index|[]
index|[]
block|{
name|dummy
block|,
name|test
block|}
control|)
block|{
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|family
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// make sure that constraints are unloaded
name|Constraints
operator|.
name|add
argument_list|(
name|desc
argument_list|,
name|RuntimeFailConstraint
operator|.
name|class
argument_list|)
expr_stmt|;
comment|// add a constraint to check to see if is run
name|Constraints
operator|.
name|add
argument_list|(
name|desc
argument_list|,
name|CheckWasRunConstraint
operator|.
name|class
argument_list|)
expr_stmt|;
name|CheckWasRunConstraint
operator|.
name|wasRun
operator|=
literal|false
expr_stmt|;
name|util
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|table
operator|.
name|setAutoFlush
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// test that we do fail on violation
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|row1
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|dummy
argument_list|,
operator|new
name|byte
index|[
literal|0
index|]
argument_list|,
literal|"pass"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"RuntimeFailConstraint wasn't triggered - this put shouldn't work!"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// NOOP
block|}
comment|// try the put again, this time constraints are not used, so it works
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
comment|// and we make sure that constraints were not run...
name|assertFalse
argument_list|(
name|CheckWasRunConstraint
operator|.
name|wasRun
argument_list|)
expr_stmt|;
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|cleanup
parameter_list|()
throws|throws
name|Exception
block|{
comment|// cleanup
name|CheckWasRunConstraint
operator|.
name|wasRun
operator|=
literal|false
expr_stmt|;
name|util
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|disableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|util
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDownAfterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|util
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
comment|/**    * Constraint to check that it was actually run (or not)    */
specifier|public
specifier|static
class|class
name|CheckWasRunConstraint
extends|extends
name|BaseConstraint
block|{
specifier|public
specifier|static
name|boolean
name|wasRun
init|=
literal|false
decl_stmt|;
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
name|wasRun
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

