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
operator|.
name|procedure
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
name|*
import|;
end_import

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
name|Date
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
name|master
operator|.
name|procedure
operator|.
name|ProcedureDescriber
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
name|Procedure
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
name|ProcedureStateSerializer
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
name|ProcedureSuspendedException
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
name|ProcedureYieldException
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
name|ByteString
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
name|BytesValue
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
name|TestProcedureDescriber
block|{
specifier|public
specifier|static
class|class
name|TestProcedure
extends|extends
name|Procedure
block|{
annotation|@
name|Override
specifier|protected
name|Procedure
index|[]
name|execute
parameter_list|(
name|Object
name|env
parameter_list|)
throws|throws
name|ProcedureYieldException
throws|,
name|ProcedureSuspendedException
throws|,
name|InterruptedException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|rollback
parameter_list|(
name|Object
name|env
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{     }
annotation|@
name|Override
specifier|protected
name|boolean
name|abort
parameter_list|(
name|Object
name|env
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|serializeStateData
parameter_list|(
name|ProcedureStateSerializer
name|serializer
parameter_list|)
throws|throws
name|IOException
block|{
name|ByteString
name|byteString
init|=
name|ByteString
operator|.
name|copyFrom
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|'A'
block|}
argument_list|)
decl_stmt|;
name|BytesValue
name|state
init|=
name|BytesValue
operator|.
name|newBuilder
argument_list|()
operator|.
name|setValue
argument_list|(
name|byteString
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|serializer
operator|.
name|serialize
argument_list|(
name|state
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|deserializeStateData
parameter_list|(
name|ProcedureStateSerializer
name|serializer
parameter_list|)
throws|throws
name|IOException
block|{     }
block|}
annotation|@
name|Test
specifier|public
name|void
name|test
parameter_list|()
block|{
name|TestProcedure
name|procedure
init|=
operator|new
name|TestProcedure
argument_list|()
decl_stmt|;
name|String
name|result
init|=
name|ProcedureDescriber
operator|.
name|describe
argument_list|(
name|procedure
argument_list|)
decl_stmt|;
name|Date
name|epoch
init|=
operator|new
name|Date
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"{ ID => '-1', PARENT_ID => '-1', STATE => 'INITIALIZING', OWNER => '', "
operator|+
literal|"TYPE => 'org.apache.hadoop.hbase.procedure.TestProcedureDescriber$TestProcedure', "
operator|+
literal|"START_TIME => '"
operator|+
name|epoch
operator|+
literal|"', LAST_UPDATE => '"
operator|+
name|epoch
operator|+
literal|"', PARAMETERS => [ "
operator|+
literal|"{ value => 'QQ==' } ] }"
argument_list|,
name|result
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

