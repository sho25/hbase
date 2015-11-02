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
name|util
package|;
end_package

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
name|procedure2
operator|.
name|util
operator|.
name|TimeoutBlockingQueue
operator|.
name|TimeoutRetriever
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
name|Assert
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
name|TestTimeoutBlockingQueue
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
name|TestTimeoutBlockingQueue
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|static
class|class
name|TestObject
block|{
specifier|private
name|long
name|timeout
decl_stmt|;
specifier|private
name|int
name|seqId
decl_stmt|;
specifier|public
name|TestObject
parameter_list|(
name|int
name|seqId
parameter_list|,
name|long
name|timeout
parameter_list|)
block|{
name|this
operator|.
name|timeout
operator|=
name|timeout
expr_stmt|;
name|this
operator|.
name|seqId
operator|=
name|seqId
expr_stmt|;
block|}
specifier|public
name|long
name|getTimeout
parameter_list|()
block|{
return|return
name|timeout
return|;
block|}
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"(%03d, %03d)"
argument_list|,
name|seqId
argument_list|,
name|timeout
argument_list|)
return|;
block|}
block|}
specifier|static
class|class
name|TestObjectTimeoutRetriever
implements|implements
name|TimeoutRetriever
argument_list|<
name|TestObject
argument_list|>
block|{
annotation|@
name|Override
specifier|public
name|long
name|getTimeout
parameter_list|(
name|TestObject
name|obj
parameter_list|)
block|{
return|return
name|obj
operator|.
name|getTimeout
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|TimeUnit
name|getTimeUnit
parameter_list|(
name|TestObject
name|obj
parameter_list|)
block|{
return|return
name|TimeUnit
operator|.
name|MILLISECONDS
return|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testOrder
parameter_list|()
block|{
name|TimeoutBlockingQueue
argument_list|<
name|TestObject
argument_list|>
name|queue
init|=
operator|new
name|TimeoutBlockingQueue
argument_list|<
name|TestObject
argument_list|>
argument_list|(
literal|8
argument_list|,
operator|new
name|TestObjectTimeoutRetriever
argument_list|()
argument_list|)
decl_stmt|;
name|long
index|[]
name|timeouts
init|=
operator|new
name|long
index|[]
block|{
literal|500
block|,
literal|200
block|,
literal|700
block|,
literal|300
block|,
literal|600
block|,
literal|600
block|,
literal|200
block|,
literal|800
block|,
literal|500
block|}
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
name|timeouts
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<=
name|i
condition|;
operator|++
name|j
control|)
block|{
name|queue
operator|.
name|add
argument_list|(
operator|new
name|TestObject
argument_list|(
name|j
argument_list|,
name|timeouts
index|[
name|j
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|queue
operator|.
name|dump
argument_list|()
expr_stmt|;
block|}
name|long
name|prev
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<=
name|i
condition|;
operator|++
name|j
control|)
block|{
name|TestObject
name|obj
init|=
name|queue
operator|.
name|poll
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|obj
operator|.
name|getTimeout
argument_list|()
operator|>=
name|prev
argument_list|)
expr_stmt|;
name|prev
operator|=
name|obj
operator|.
name|getTimeout
argument_list|()
expr_stmt|;
name|queue
operator|.
name|dump
argument_list|()
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTimeoutBlockingQueue
parameter_list|()
block|{
name|TimeoutBlockingQueue
argument_list|<
name|TestObject
argument_list|>
name|queue
decl_stmt|;
name|int
index|[]
index|[]
name|testArray
init|=
operator|new
name|int
index|[]
index|[]
block|{
block|{
literal|200
block|,
literal|400
block|,
literal|600
block|}
block|,
comment|// append
block|{
literal|200
block|,
literal|400
block|,
literal|100
block|}
block|,
comment|// prepend
block|{
literal|200
block|,
literal|400
block|,
literal|300
block|}
block|,
comment|// insert
block|}
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
name|testArray
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|int
index|[]
name|sortedArray
init|=
name|Arrays
operator|.
name|copyOf
argument_list|(
name|testArray
index|[
name|i
index|]
argument_list|,
name|testArray
index|[
name|i
index|]
operator|.
name|length
argument_list|)
decl_stmt|;
name|Arrays
operator|.
name|sort
argument_list|(
name|sortedArray
argument_list|)
expr_stmt|;
comment|// test with head == 0
name|queue
operator|=
operator|new
name|TimeoutBlockingQueue
argument_list|<
name|TestObject
argument_list|>
argument_list|(
literal|2
argument_list|,
operator|new
name|TestObjectTimeoutRetriever
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|testArray
index|[
name|i
index|]
operator|.
name|length
condition|;
operator|++
name|j
control|)
block|{
name|queue
operator|.
name|add
argument_list|(
operator|new
name|TestObject
argument_list|(
name|j
argument_list|,
name|testArray
index|[
name|i
index|]
index|[
name|j
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|queue
operator|.
name|dump
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
operator|!
name|queue
operator|.
name|isEmpty
argument_list|()
condition|;
operator|++
name|j
control|)
block|{
name|assertEquals
argument_list|(
name|sortedArray
index|[
name|j
index|]
argument_list|,
name|queue
operator|.
name|poll
argument_list|()
operator|.
name|getTimeout
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|queue
operator|=
operator|new
name|TimeoutBlockingQueue
argument_list|<
name|TestObject
argument_list|>
argument_list|(
literal|2
argument_list|,
operator|new
name|TestObjectTimeoutRetriever
argument_list|()
argument_list|)
expr_stmt|;
name|queue
operator|.
name|add
argument_list|(
operator|new
name|TestObject
argument_list|(
literal|0
argument_list|,
literal|50
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|50
argument_list|,
name|queue
operator|.
name|poll
argument_list|()
operator|.
name|getTimeout
argument_list|()
argument_list|)
expr_stmt|;
comment|// test with head> 0
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|testArray
index|[
name|i
index|]
operator|.
name|length
condition|;
operator|++
name|j
control|)
block|{
name|queue
operator|.
name|add
argument_list|(
operator|new
name|TestObject
argument_list|(
name|j
argument_list|,
name|testArray
index|[
name|i
index|]
index|[
name|j
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|queue
operator|.
name|dump
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
operator|!
name|queue
operator|.
name|isEmpty
argument_list|()
condition|;
operator|++
name|j
control|)
block|{
name|assertEquals
argument_list|(
name|sortedArray
index|[
name|j
index|]
argument_list|,
name|queue
operator|.
name|poll
argument_list|()
operator|.
name|getTimeout
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

