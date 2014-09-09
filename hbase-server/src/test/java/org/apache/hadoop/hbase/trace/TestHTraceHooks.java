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
name|trace
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
name|assertNotNull
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
name|Waiter
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
name|Table
import|;
end_import

begin_import
import|import
name|org
operator|.
name|htrace
operator|.
name|Sampler
import|;
end_import

begin_import
import|import
name|org
operator|.
name|htrace
operator|.
name|Span
import|;
end_import

begin_import
import|import
name|org
operator|.
name|htrace
operator|.
name|Trace
import|;
end_import

begin_import
import|import
name|org
operator|.
name|htrace
operator|.
name|TraceScope
import|;
end_import

begin_import
import|import
name|org
operator|.
name|htrace
operator|.
name|TraceTree
import|;
end_import

begin_import
import|import
name|org
operator|.
name|htrace
operator|.
name|impl
operator|.
name|POJOSpanReceiver
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

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Multimap
import|;
end_import

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
name|TestHTraceHooks
block|{
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|FAMILY_BYTES
init|=
literal|"family"
operator|.
name|getBytes
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|POJOSpanReceiver
name|rcvr
init|=
operator|new
name|POJOSpanReceiver
argument_list|()
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|before
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|2
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|Trace
operator|.
name|addReceiver
argument_list|(
name|rcvr
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|after
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
name|Trace
operator|.
name|removeReceiver
argument_list|(
name|rcvr
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTraceCreateTable
parameter_list|()
throws|throws
name|Exception
block|{
name|TraceScope
name|tableCreationSpan
init|=
name|Trace
operator|.
name|startSpan
argument_list|(
literal|"creating table"
argument_list|,
name|Sampler
operator|.
name|ALWAYS
argument_list|)
decl_stmt|;
name|Table
name|table
decl_stmt|;
try|try
block|{
name|table
operator|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
literal|"table"
operator|.
name|getBytes
argument_list|()
argument_list|,
name|FAMILY_BYTES
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|tableCreationSpan
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|// Some table creation is async.  Need to make sure that everything is full in before
comment|// checking to see if the spans are there.
name|TEST_UTIL
operator|.
name|waitFor
argument_list|(
literal|1000
argument_list|,
operator|new
name|Waiter
operator|.
name|Predicate
argument_list|<
name|Exception
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|evaluate
parameter_list|()
throws|throws
name|Exception
block|{
return|return
name|rcvr
operator|.
name|getSpans
argument_list|()
operator|.
name|size
argument_list|()
operator|>=
literal|5
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|Collection
argument_list|<
name|Span
argument_list|>
name|spans
init|=
name|rcvr
operator|.
name|getSpans
argument_list|()
decl_stmt|;
name|TraceTree
name|traceTree
init|=
operator|new
name|TraceTree
argument_list|(
name|spans
argument_list|)
decl_stmt|;
name|Collection
argument_list|<
name|Span
argument_list|>
name|roots
init|=
name|traceTree
operator|.
name|getRoots
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|roots
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Span
name|createTableRoot
init|=
name|roots
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"creating table"
argument_list|,
name|createTableRoot
operator|.
name|getDescription
argument_list|()
argument_list|)
expr_stmt|;
name|Multimap
argument_list|<
name|Long
argument_list|,
name|Span
argument_list|>
name|spansByParentIdMap
init|=
name|traceTree
operator|.
name|getSpansByParentIdMap
argument_list|()
decl_stmt|;
name|int
name|createTableCount
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Span
name|s
range|:
name|spansByParentIdMap
operator|.
name|get
argument_list|(
name|createTableRoot
operator|.
name|getSpanId
argument_list|()
argument_list|)
control|)
block|{
if|if
condition|(
name|s
operator|.
name|getDescription
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"MasterService.CreateTable"
argument_list|)
condition|)
block|{
name|createTableCount
operator|++
expr_stmt|;
block|}
block|}
name|assertTrue
argument_list|(
name|createTableCount
operator|>=
literal|1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|spansByParentIdMap
operator|.
name|get
argument_list|(
name|createTableRoot
operator|.
name|getSpanId
argument_list|()
argument_list|)
operator|.
name|size
argument_list|()
operator|>
literal|3
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|spans
operator|.
name|size
argument_list|()
operator|>
literal|5
argument_list|)
expr_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
literal|"row"
operator|.
name|getBytes
argument_list|()
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|FAMILY_BYTES
argument_list|,
literal|"col"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|"value"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|TraceScope
name|putSpan
init|=
name|Trace
operator|.
name|startSpan
argument_list|(
literal|"doing put"
argument_list|,
name|Sampler
operator|.
name|ALWAYS
argument_list|)
decl_stmt|;
try|try
block|{
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|putSpan
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|spans
operator|=
name|rcvr
operator|.
name|getSpans
argument_list|()
expr_stmt|;
name|traceTree
operator|=
operator|new
name|TraceTree
argument_list|(
name|spans
argument_list|)
expr_stmt|;
name|roots
operator|=
name|traceTree
operator|.
name|getRoots
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|roots
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Span
name|putRoot
init|=
literal|null
decl_stmt|;
for|for
control|(
name|Span
name|root
range|:
name|roots
control|)
block|{
if|if
condition|(
name|root
operator|.
name|getDescription
argument_list|()
operator|.
name|equals
argument_list|(
literal|"doing put"
argument_list|)
condition|)
block|{
name|putRoot
operator|=
name|root
expr_stmt|;
block|}
block|}
name|assertNotNull
argument_list|(
name|putRoot
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

