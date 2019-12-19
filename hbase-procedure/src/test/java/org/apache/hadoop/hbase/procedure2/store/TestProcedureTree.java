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
operator|.
name|store
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
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|UncheckedIOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
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
name|ProcedureUtil
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
name|procedure2
operator|.
name|store
operator|.
name|ProcedureStore
operator|.
name|ProcedureIterator
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
name|TestProcedureTree
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
name|TestProcedureTree
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
class|class
name|TestProcedure
extends|extends
name|Procedure
argument_list|<
name|Void
argument_list|>
block|{
annotation|@
name|Override
specifier|public
name|void
name|setProcId
parameter_list|(
name|long
name|procId
parameter_list|)
block|{
name|super
operator|.
name|setProcId
argument_list|(
name|procId
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setParentProcId
parameter_list|(
name|long
name|parentProcId
parameter_list|)
block|{
name|super
operator|.
name|setParentProcId
argument_list|(
name|parentProcId
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|addStackIndex
parameter_list|(
name|int
name|index
parameter_list|)
block|{
name|super
operator|.
name|addStackIndex
argument_list|(
name|index
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|Procedure
argument_list|<
name|Void
argument_list|>
index|[]
name|execute
parameter_list|(
name|Void
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
name|Void
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
name|Void
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
block|{     }
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
specifier|private
name|TestProcedure
name|createProc
parameter_list|(
name|long
name|procId
parameter_list|,
name|long
name|parentProcId
parameter_list|)
block|{
name|TestProcedure
name|proc
init|=
operator|new
name|TestProcedure
argument_list|()
decl_stmt|;
name|proc
operator|.
name|setProcId
argument_list|(
name|procId
argument_list|)
expr_stmt|;
if|if
condition|(
name|parentProcId
operator|!=
name|Procedure
operator|.
name|NO_PROC_ID
condition|)
block|{
name|proc
operator|.
name|setParentProcId
argument_list|(
name|parentProcId
argument_list|)
expr_stmt|;
block|}
return|return
name|proc
return|;
block|}
specifier|private
name|List
argument_list|<
name|ProcedureProtos
operator|.
name|Procedure
argument_list|>
name|toProtos
parameter_list|(
name|TestProcedure
modifier|...
name|procs
parameter_list|)
block|{
return|return
name|Arrays
operator|.
name|stream
argument_list|(
name|procs
argument_list|)
operator|.
name|map
argument_list|(
name|p
lambda|->
block|{
try|try
block|{
return|return
name|ProcedureUtil
operator|.
name|convertToProtoProcedure
argument_list|(
name|p
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|UncheckedIOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
return|;
block|}
specifier|private
name|List
argument_list|<
name|TestProcedure
argument_list|>
name|getProcs
parameter_list|(
name|ProcedureIterator
name|iter
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|TestProcedure
argument_list|>
name|procs
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
while|while
condition|(
name|iter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|procs
operator|.
name|add
argument_list|(
operator|(
name|TestProcedure
operator|)
name|iter
operator|.
name|next
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|procs
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMissingStackId
parameter_list|()
throws|throws
name|IOException
block|{
name|TestProcedure
name|proc0
init|=
name|createProc
argument_list|(
literal|1
argument_list|,
name|Procedure
operator|.
name|NO_PROC_ID
argument_list|)
decl_stmt|;
name|proc0
operator|.
name|addStackIndex
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|TestProcedure
name|proc1
init|=
name|createProc
argument_list|(
literal|2
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|proc1
operator|.
name|addStackIndex
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|TestProcedure
name|proc2
init|=
name|createProc
argument_list|(
literal|3
argument_list|,
literal|2
argument_list|)
decl_stmt|;
name|proc2
operator|.
name|addStackIndex
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|ProcedureTree
name|tree
init|=
name|ProcedureTree
operator|.
name|build
argument_list|(
name|toProtos
argument_list|(
name|proc0
argument_list|,
name|proc1
argument_list|,
name|proc2
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|TestProcedure
argument_list|>
name|validProcs
init|=
name|getProcs
argument_list|(
name|tree
operator|.
name|getValidProcs
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|validProcs
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|TestProcedure
argument_list|>
name|corruptedProcs
init|=
name|getProcs
argument_list|(
name|tree
operator|.
name|getCorruptedProcs
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|corruptedProcs
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|corruptedProcs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getProcId
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|corruptedProcs
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getProcId
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|corruptedProcs
operator|.
name|get
argument_list|(
literal|2
argument_list|)
operator|.
name|getProcId
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDuplicatedStackId
parameter_list|()
throws|throws
name|IOException
block|{
name|TestProcedure
name|proc0
init|=
name|createProc
argument_list|(
literal|1
argument_list|,
name|Procedure
operator|.
name|NO_PROC_ID
argument_list|)
decl_stmt|;
name|proc0
operator|.
name|addStackIndex
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|TestProcedure
name|proc1
init|=
name|createProc
argument_list|(
literal|2
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|proc1
operator|.
name|addStackIndex
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|TestProcedure
name|proc2
init|=
name|createProc
argument_list|(
literal|3
argument_list|,
literal|2
argument_list|)
decl_stmt|;
name|proc2
operator|.
name|addStackIndex
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|ProcedureTree
name|tree
init|=
name|ProcedureTree
operator|.
name|build
argument_list|(
name|toProtos
argument_list|(
name|proc0
argument_list|,
name|proc1
argument_list|,
name|proc2
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|TestProcedure
argument_list|>
name|validProcs
init|=
name|getProcs
argument_list|(
name|tree
operator|.
name|getValidProcs
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|validProcs
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|TestProcedure
argument_list|>
name|corruptedProcs
init|=
name|getProcs
argument_list|(
name|tree
operator|.
name|getCorruptedProcs
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|corruptedProcs
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|corruptedProcs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getProcId
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|corruptedProcs
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getProcId
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|corruptedProcs
operator|.
name|get
argument_list|(
literal|2
argument_list|)
operator|.
name|getProcId
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testOrphan
parameter_list|()
throws|throws
name|IOException
block|{
name|TestProcedure
name|proc0
init|=
name|createProc
argument_list|(
literal|1
argument_list|,
name|Procedure
operator|.
name|NO_PROC_ID
argument_list|)
decl_stmt|;
name|proc0
operator|.
name|addStackIndex
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|TestProcedure
name|proc1
init|=
name|createProc
argument_list|(
literal|2
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|proc1
operator|.
name|addStackIndex
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|TestProcedure
name|proc2
init|=
name|createProc
argument_list|(
literal|3
argument_list|,
name|Procedure
operator|.
name|NO_PROC_ID
argument_list|)
decl_stmt|;
name|proc2
operator|.
name|addStackIndex
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|TestProcedure
name|proc3
init|=
name|createProc
argument_list|(
literal|5
argument_list|,
literal|4
argument_list|)
decl_stmt|;
name|proc3
operator|.
name|addStackIndex
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|ProcedureTree
name|tree
init|=
name|ProcedureTree
operator|.
name|build
argument_list|(
name|toProtos
argument_list|(
name|proc0
argument_list|,
name|proc1
argument_list|,
name|proc2
argument_list|,
name|proc3
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|TestProcedure
argument_list|>
name|validProcs
init|=
name|getProcs
argument_list|(
name|tree
operator|.
name|getValidProcs
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|validProcs
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|TestProcedure
argument_list|>
name|corruptedProcs
init|=
name|getProcs
argument_list|(
name|tree
operator|.
name|getCorruptedProcs
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|corruptedProcs
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|corruptedProcs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getProcId
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|corruptedProcs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getParentProcId
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
