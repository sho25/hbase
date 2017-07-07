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
name|ProcedureInfo
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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|util
operator|.
name|JsonFormat
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
name|testclassification
operator|.
name|MasterTests
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
comment|// remove the state-data from the procedure protobuf to compare it to the gen ProcedureInfo
specifier|final
name|ProcedureProtos
operator|.
name|Procedure
name|pbproc
init|=
name|proto2
operator|.
name|toBuilder
argument_list|()
operator|.
name|clearStateData
argument_list|()
operator|.
name|build
argument_list|()
decl_stmt|;
comment|// check ProcedureInfo to protobuf conversion
specifier|final
name|ProcedureInfo
name|protoInfo1
init|=
name|ProcedureUtil
operator|.
name|convertToProcedureInfo
argument_list|(
name|proc1
argument_list|)
decl_stmt|;
specifier|final
name|ProcedureProtos
operator|.
name|Procedure
name|proto3
init|=
name|ProcedureUtil
operator|.
name|convertToProtoProcedure
argument_list|(
name|protoInfo1
argument_list|)
decl_stmt|;
specifier|final
name|ProcedureInfo
name|protoInfo2
init|=
name|ProcedureUtil
operator|.
name|convertToProcedureInfo
argument_list|(
name|proto3
argument_list|)
decl_stmt|;
specifier|final
name|ProcedureProtos
operator|.
name|Procedure
name|proto4
init|=
name|ProcedureUtil
operator|.
name|convertToProtoProcedure
argument_list|(
name|protoInfo2
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"ProcedureInfo protobuf does not match"
argument_list|,
name|proto3
argument_list|,
name|proto4
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"ProcedureInfo/Procedure protobuf does not match"
argument_list|,
name|pbproc
argument_list|,
name|proto3
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"ProcedureInfo/Procedure protobuf does not match"
argument_list|,
name|pbproc
argument_list|,
name|proto4
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
specifier|public
specifier|static
name|void
name|main
parameter_list|(
specifier|final
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
specifier|final
name|TestProcedure
name|proc1
init|=
operator|new
name|TestProcedure
argument_list|(
literal|10
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
name|JsonFormat
operator|.
name|Printer
name|printer
init|=
name|JsonFormat
operator|.
name|printer
argument_list|()
operator|.
name|omittingInsignificantWhitespace
argument_list|()
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|printer
operator|.
name|print
argument_list|(
name|proto1
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

