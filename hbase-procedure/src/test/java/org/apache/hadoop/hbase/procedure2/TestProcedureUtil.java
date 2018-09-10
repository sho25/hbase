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
name|procedure2
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
name|procedure2
operator|.
name|ProcedureTestingUtility
operator|.
name|TestProcedure
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
name|MasterTests
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|ProcedureProtos
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MasterTests
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
name|TestProcedureUtil
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
name|TestProcedureUtil
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testValidation
parameter_list|()
throws|throws
name|Exception
block|{
name|ProcedureUtil
operator|.
name|validateClass
argument_list|(
operator|new
name|TestProcedure
argument_list|(
literal|10
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|BadProcedureException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testNoDefaultConstructorValidation
parameter_list|()
throws|throws
name|Exception
block|{
name|ProcedureUtil
operator|.
name|validateClass
argument_list|(
operator|new
name|TestProcedureNoDefaultConstructor
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testConvert
parameter_list|()
throws|throws
name|Exception
block|{
comment|// check Procedure to protobuf conversion
specifier|final
name|TestProcedure
name|proc1
init|=
operator|new
name|TestProcedure
argument_list|(
literal|10
argument_list|,
literal|1
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|65
block|}
argument_list|)
decl_stmt|;
specifier|final
name|ProcedureProtos
operator|.
name|Procedure
name|proto1
init|=
name|ProcedureUtil
operator|.
name|convertToProtoProcedure
argument_list|(
name|proc1
argument_list|)
decl_stmt|;
specifier|final
name|TestProcedure
name|proc2
init|=
operator|(
name|TestProcedure
operator|)
name|ProcedureUtil
operator|.
name|convertToProcedure
argument_list|(
name|proto1
argument_list|)
decl_stmt|;
specifier|final
name|ProcedureProtos
operator|.
name|Procedure
name|proto2
init|=
name|ProcedureUtil
operator|.
name|convertToProtoProcedure
argument_list|(
name|proc2
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|proto2
operator|.
name|hasResult
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Procedure protobuf does not match"
argument_list|,
name|proto1
argument_list|,
name|proto2
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetBackoffTimeMs
parameter_list|()
block|{
for|for
control|(
name|int
name|i
init|=
literal|30
init|;
name|i
operator|<
literal|1000
condition|;
name|i
operator|++
control|)
block|{
name|assertEquals
argument_list|(
name|TimeUnit
operator|.
name|MINUTES
operator|.
name|toMillis
argument_list|(
literal|10
argument_list|)
argument_list|,
name|ProcedureUtil
operator|.
name|getBackoffTimeMs
argument_list|(
literal|30
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|1000
argument_list|,
name|ProcedureUtil
operator|.
name|getBackoffTimeMs
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2000
argument_list|,
name|ProcedureUtil
operator|.
name|getBackoffTimeMs
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|32000
argument_list|,
name|ProcedureUtil
operator|.
name|getBackoffTimeMs
argument_list|(
literal|5
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
class|class
name|TestProcedureNoDefaultConstructor
extends|extends
name|TestProcedure
block|{
specifier|public
name|TestProcedureNoDefaultConstructor
parameter_list|(
name|int
name|x
parameter_list|)
block|{}
block|}
block|}
end_class

end_unit

