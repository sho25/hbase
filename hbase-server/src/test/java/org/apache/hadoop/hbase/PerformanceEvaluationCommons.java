begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
import|import
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
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
name|List
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/**  * Code shared by PE tests.  */
end_comment

begin_class
specifier|public
class|class
name|PerformanceEvaluationCommons
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|PerformanceEvaluationCommons
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|void
name|assertValueSize
parameter_list|(
specifier|final
name|int
name|expectedSize
parameter_list|,
specifier|final
name|int
name|got
parameter_list|)
block|{
if|if
condition|(
name|got
operator|!=
name|expectedSize
condition|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Expected "
operator|+
name|expectedSize
operator|+
literal|" but got "
operator|+
name|got
argument_list|)
throw|;
block|}
block|}
specifier|public
specifier|static
name|void
name|assertKey
parameter_list|(
specifier|final
name|byte
index|[]
name|expected
parameter_list|,
specifier|final
name|ByteBuffer
name|got
parameter_list|)
block|{
name|byte
index|[]
name|b
init|=
operator|new
name|byte
index|[
name|got
operator|.
name|limit
argument_list|()
index|]
decl_stmt|;
name|got
operator|.
name|get
argument_list|(
name|b
argument_list|,
literal|0
argument_list|,
name|got
operator|.
name|limit
argument_list|()
argument_list|)
expr_stmt|;
name|assertKey
argument_list|(
name|expected
argument_list|,
name|b
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|assertKey
parameter_list|(
specifier|final
name|byte
index|[]
name|expected
parameter_list|,
specifier|final
name|Cell
name|c
parameter_list|)
block|{
name|assertKey
argument_list|(
name|expected
argument_list|,
name|c
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|c
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|c
operator|.
name|getRowLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|assertKey
parameter_list|(
specifier|final
name|byte
index|[]
name|expected
parameter_list|,
specifier|final
name|byte
index|[]
name|got
parameter_list|)
block|{
name|assertKey
argument_list|(
name|expected
argument_list|,
name|got
argument_list|,
literal|0
argument_list|,
name|got
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|assertKey
parameter_list|(
specifier|final
name|byte
index|[]
name|expected
parameter_list|,
specifier|final
name|byte
index|[]
name|gotArray
parameter_list|,
specifier|final
name|int
name|gotArrayOffset
parameter_list|,
specifier|final
name|int
name|gotArrayLength
parameter_list|)
block|{
if|if
condition|(
operator|!
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
operator|.
name|equals
argument_list|(
name|expected
argument_list|,
literal|0
argument_list|,
name|expected
operator|.
name|length
argument_list|,
name|gotArray
argument_list|,
name|gotArrayOffset
argument_list|,
name|gotArrayLength
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Expected "
operator|+
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
operator|.
name|toString
argument_list|(
name|expected
argument_list|)
operator|+
literal|" but got "
operator|+
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
operator|.
name|toString
argument_list|(
name|gotArray
argument_list|,
name|gotArrayOffset
argument_list|,
name|gotArrayLength
argument_list|)
argument_list|)
throw|;
block|}
block|}
specifier|public
specifier|static
name|void
name|concurrentReads
parameter_list|(
specifier|final
name|Runnable
name|r
parameter_list|)
block|{
specifier|final
name|int
name|count
init|=
literal|1
decl_stmt|;
name|long
name|now
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Thread
argument_list|>
name|threads
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|count
argument_list|)
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
name|count
condition|;
name|i
operator|++
control|)
block|{
name|threads
operator|.
name|add
argument_list|(
operator|new
name|Thread
argument_list|(
name|r
argument_list|,
literal|"concurrentRead-"
operator|+
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Thread
name|t
range|:
name|threads
control|)
block|{
name|t
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|Thread
name|t
range|:
name|threads
control|)
block|{
try|try
block|{
name|t
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Test took "
operator|+
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|now
operator|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

