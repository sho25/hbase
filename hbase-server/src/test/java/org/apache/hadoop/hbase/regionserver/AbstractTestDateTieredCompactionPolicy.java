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
name|regionserver
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|regionserver
operator|.
name|compactions
operator|.
name|DateTieredCompactionPolicy
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
name|regionserver
operator|.
name|compactions
operator|.
name|DateTieredCompactionRequest
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
name|EnvironmentEdgeManager
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
name|ManualEnvironmentEdge
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
name|common
operator|.
name|collect
operator|.
name|ImmutableList
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
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_class
specifier|public
class|class
name|AbstractTestDateTieredCompactionPolicy
extends|extends
name|TestCompactionPolicy
block|{
specifier|protected
name|ArrayList
argument_list|<
name|HStoreFile
argument_list|>
name|sfCreate
parameter_list|(
name|long
index|[]
name|minTimestamps
parameter_list|,
name|long
index|[]
name|maxTimestamps
parameter_list|,
name|long
index|[]
name|sizes
parameter_list|)
throws|throws
name|IOException
block|{
name|ManualEnvironmentEdge
name|timeMachine
init|=
operator|new
name|ManualEnvironmentEdge
argument_list|()
decl_stmt|;
name|EnvironmentEdgeManager
operator|.
name|injectEdge
argument_list|(
name|timeMachine
argument_list|)
expr_stmt|;
comment|// Has to be> 0 and< now.
name|timeMachine
operator|.
name|setValue
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|ArrayList
argument_list|<
name|Long
argument_list|>
name|ageInDisk
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|sizes
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|ageInDisk
operator|.
name|add
argument_list|(
literal|0L
argument_list|)
expr_stmt|;
block|}
name|ArrayList
argument_list|<
name|HStoreFile
argument_list|>
name|ret
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|sizes
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|MockHStoreFile
name|msf
init|=
operator|new
name|MockHStoreFile
argument_list|(
name|TEST_UTIL
argument_list|,
name|TEST_FILE
argument_list|,
name|sizes
index|[
name|i
index|]
argument_list|,
name|ageInDisk
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
literal|false
argument_list|,
name|i
argument_list|)
decl_stmt|;
name|msf
operator|.
name|setTimeRangeTracker
argument_list|(
name|TimeRangeTracker
operator|.
name|create
argument_list|(
name|TimeRangeTracker
operator|.
name|Type
operator|.
name|SYNC
argument_list|,
name|minTimestamps
index|[
name|i
index|]
argument_list|,
name|maxTimestamps
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|ret
operator|.
name|add
argument_list|(
name|msf
argument_list|)
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
specifier|protected
name|void
name|compactEquals
parameter_list|(
name|long
name|now
parameter_list|,
name|ArrayList
argument_list|<
name|HStoreFile
argument_list|>
name|candidates
parameter_list|,
name|long
index|[]
name|expectedFileSizes
parameter_list|,
name|long
index|[]
name|expectedBoundaries
parameter_list|,
name|boolean
name|isMajor
parameter_list|,
name|boolean
name|toCompact
parameter_list|)
throws|throws
name|IOException
block|{
name|ManualEnvironmentEdge
name|timeMachine
init|=
operator|new
name|ManualEnvironmentEdge
argument_list|()
decl_stmt|;
name|EnvironmentEdgeManager
operator|.
name|injectEdge
argument_list|(
name|timeMachine
argument_list|)
expr_stmt|;
name|timeMachine
operator|.
name|setValue
argument_list|(
name|now
argument_list|)
expr_stmt|;
name|DateTieredCompactionRequest
name|request
decl_stmt|;
name|DateTieredCompactionPolicy
name|policy
init|=
operator|(
name|DateTieredCompactionPolicy
operator|)
name|store
operator|.
name|storeEngine
operator|.
name|getCompactionPolicy
argument_list|()
decl_stmt|;
if|if
condition|(
name|isMajor
condition|)
block|{
for|for
control|(
name|HStoreFile
name|file
range|:
name|candidates
control|)
block|{
operator|(
operator|(
name|MockHStoreFile
operator|)
name|file
operator|)
operator|.
name|setIsMajor
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|toCompact
argument_list|,
name|policy
operator|.
name|shouldPerformMajorCompaction
argument_list|(
name|candidates
argument_list|)
argument_list|)
expr_stmt|;
name|request
operator|=
operator|(
name|DateTieredCompactionRequest
operator|)
name|policy
operator|.
name|selectMajorCompaction
argument_list|(
name|candidates
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertEquals
argument_list|(
name|toCompact
argument_list|,
name|policy
operator|.
name|needsCompaction
argument_list|(
name|candidates
argument_list|,
name|ImmutableList
operator|.
name|of
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|request
operator|=
operator|(
name|DateTieredCompactionRequest
operator|)
name|policy
operator|.
name|selectMinorCompaction
argument_list|(
name|candidates
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|HStoreFile
argument_list|>
name|actual
init|=
name|Lists
operator|.
name|newArrayList
argument_list|(
name|request
operator|.
name|getFiles
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|toString
argument_list|(
name|expectedFileSizes
argument_list|)
argument_list|,
name|Arrays
operator|.
name|toString
argument_list|(
name|getSizes
argument_list|(
name|actual
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|toString
argument_list|(
name|expectedBoundaries
argument_list|)
argument_list|,
name|Arrays
operator|.
name|toString
argument_list|(
name|request
operator|.
name|getBoundaries
argument_list|()
operator|.
name|toArray
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

