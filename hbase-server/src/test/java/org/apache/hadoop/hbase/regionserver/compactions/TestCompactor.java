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
operator|.
name|compactions
package|;
end_package

begin_import
import|import static
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
name|StripeStoreFileManager
operator|.
name|STRIPE_END_KEY
import|;
end_import

begin_import
import|import static
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
name|StripeStoreFileManager
operator|.
name|STRIPE_START_KEY
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
name|assertArrayEquals
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
name|assertNull
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
name|mockito
operator|.
name|Matchers
operator|.
name|any
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Matchers
operator|.
name|anyBoolean
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Matchers
operator|.
name|anyLong
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|doAnswer
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|mock
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|when
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
name|java
operator|.
name|util
operator|.
name|TreeMap
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
name|fs
operator|.
name|Path
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
name|Cell
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
name|KeyValue
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
name|io
operator|.
name|hfile
operator|.
name|HFile
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
name|BloomType
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
name|InternalScanner
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
name|ScannerContext
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
name|StoreFile
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
name|StoreFileReader
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
name|StoreFileScanner
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
name|StoreFileWriter
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
name|StripeMultiFileWriter
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
name|mockito
operator|.
name|invocation
operator|.
name|InvocationOnMock
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|stubbing
operator|.
name|Answer
import|;
end_import

begin_class
specifier|public
class|class
name|TestCompactor
block|{
specifier|public
specifier|static
name|StoreFile
name|createDummyStoreFile
parameter_list|(
name|long
name|maxSequenceId
parameter_list|)
throws|throws
name|Exception
block|{
comment|// "Files" are totally unused, it's Scanner class below that gives compactor fake KVs.
comment|// But compaction depends on everything under the sun, so stub everything with dummies.
name|StoreFile
name|sf
init|=
name|mock
argument_list|(
name|StoreFile
operator|.
name|class
argument_list|)
decl_stmt|;
name|StoreFileReader
name|r
init|=
name|mock
argument_list|(
name|StoreFileReader
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|r
operator|.
name|length
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|1L
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|r
operator|.
name|getBloomFilterType
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|BloomType
operator|.
name|NONE
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|r
operator|.
name|getHFileReader
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|mock
argument_list|(
name|HFile
operator|.
name|Reader
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|r
operator|.
name|getStoreFileScanner
argument_list|(
name|anyBoolean
argument_list|()
argument_list|,
name|anyBoolean
argument_list|()
argument_list|,
name|anyBoolean
argument_list|()
argument_list|,
name|anyLong
argument_list|()
argument_list|,
name|anyLong
argument_list|()
argument_list|,
name|anyBoolean
argument_list|()
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|mock
argument_list|(
name|StoreFileScanner
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|sf
operator|.
name|getReader
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|r
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|sf
operator|.
name|createReader
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|r
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|sf
operator|.
name|createReader
argument_list|(
name|anyBoolean
argument_list|()
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|r
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|sf
operator|.
name|cloneForReader
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|sf
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|sf
operator|.
name|getMaxSequenceId
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|maxSequenceId
argument_list|)
expr_stmt|;
return|return
name|sf
return|;
block|}
specifier|public
specifier|static
name|CompactionRequest
name|createDummyRequest
parameter_list|()
throws|throws
name|Exception
block|{
return|return
operator|new
name|CompactionRequest
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|createDummyStoreFile
argument_list|(
literal|1L
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
comment|// StoreFile.Writer has private ctor and is unwieldy, so this has to be convoluted.
specifier|public
specifier|static
class|class
name|StoreFileWritersCapture
implements|implements
name|Answer
argument_list|<
name|StoreFileWriter
argument_list|>
implements|,
name|StripeMultiFileWriter
operator|.
name|WriterFactory
block|{
specifier|public
specifier|static
class|class
name|Writer
block|{
specifier|public
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
name|kvs
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
name|TreeMap
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
name|data
init|=
operator|new
name|TreeMap
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
specifier|public
name|boolean
name|hasMetadata
decl_stmt|;
block|}
specifier|private
name|List
argument_list|<
name|Writer
argument_list|>
name|writers
init|=
operator|new
name|ArrayList
argument_list|<
name|Writer
argument_list|>
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|StoreFileWriter
name|createWriter
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|Writer
name|realWriter
init|=
operator|new
name|Writer
argument_list|()
decl_stmt|;
name|writers
operator|.
name|add
argument_list|(
name|realWriter
argument_list|)
expr_stmt|;
name|StoreFileWriter
name|writer
init|=
name|mock
argument_list|(
name|StoreFileWriter
operator|.
name|class
argument_list|)
decl_stmt|;
name|doAnswer
argument_list|(
operator|new
name|Answer
argument_list|<
name|Object
argument_list|>
argument_list|()
block|{
specifier|public
name|Object
name|answer
parameter_list|(
name|InvocationOnMock
name|invocation
parameter_list|)
block|{
return|return
name|realWriter
operator|.
name|kvs
operator|.
name|add
argument_list|(
operator|(
name|KeyValue
operator|)
name|invocation
operator|.
name|getArguments
argument_list|()
index|[
literal|0
index|]
argument_list|)
return|;
block|}
block|}
argument_list|)
operator|.
name|when
argument_list|(
name|writer
argument_list|)
operator|.
name|append
argument_list|(
name|any
argument_list|(
name|KeyValue
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|doAnswer
argument_list|(
operator|new
name|Answer
argument_list|<
name|Object
argument_list|>
argument_list|()
block|{
specifier|public
name|Object
name|answer
parameter_list|(
name|InvocationOnMock
name|invocation
parameter_list|)
block|{
name|Object
index|[]
name|args
init|=
name|invocation
operator|.
name|getArguments
argument_list|()
decl_stmt|;
return|return
name|realWriter
operator|.
name|data
operator|.
name|put
argument_list|(
operator|(
name|byte
index|[]
operator|)
name|args
index|[
literal|0
index|]
argument_list|,
operator|(
name|byte
index|[]
operator|)
name|args
index|[
literal|1
index|]
argument_list|)
return|;
block|}
block|}
argument_list|)
operator|.
name|when
argument_list|(
name|writer
argument_list|)
operator|.
name|appendFileInfo
argument_list|(
name|any
argument_list|(
name|byte
index|[]
operator|.
expr|class
argument_list|)
argument_list|,
name|any
argument_list|(
name|byte
index|[]
operator|.
expr|class
argument_list|)
argument_list|)
expr_stmt|;
name|doAnswer
argument_list|(
operator|new
name|Answer
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|answer
parameter_list|(
name|InvocationOnMock
name|invocation
parameter_list|)
throws|throws
name|Throwable
block|{
name|realWriter
operator|.
name|hasMetadata
operator|=
literal|true
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
operator|.
name|when
argument_list|(
name|writer
argument_list|)
operator|.
name|appendMetadata
argument_list|(
name|any
argument_list|(
name|long
operator|.
name|class
argument_list|)
argument_list|,
name|any
argument_list|(
name|boolean
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|doAnswer
argument_list|(
operator|new
name|Answer
argument_list|<
name|Path
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Path
name|answer
parameter_list|(
name|InvocationOnMock
name|invocation
parameter_list|)
throws|throws
name|Throwable
block|{
return|return
operator|new
name|Path
argument_list|(
literal|"foo"
argument_list|)
return|;
block|}
block|}
argument_list|)
operator|.
name|when
argument_list|(
name|writer
argument_list|)
operator|.
name|getPath
argument_list|()
expr_stmt|;
return|return
name|writer
return|;
block|}
annotation|@
name|Override
specifier|public
name|StoreFileWriter
name|answer
parameter_list|(
name|InvocationOnMock
name|invocation
parameter_list|)
throws|throws
name|Throwable
block|{
return|return
name|createWriter
argument_list|()
return|;
block|}
specifier|public
name|void
name|verifyKvs
parameter_list|(
name|KeyValue
index|[]
index|[]
name|kvss
parameter_list|,
name|boolean
name|allFiles
parameter_list|,
name|boolean
name|requireMetadata
parameter_list|)
block|{
if|if
condition|(
name|allFiles
condition|)
block|{
name|assertEquals
argument_list|(
name|kvss
operator|.
name|length
argument_list|,
name|writers
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|int
name|skippedWriters
init|=
literal|0
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
name|kvss
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|KeyValue
index|[]
name|kvs
init|=
name|kvss
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|kvs
operator|!=
literal|null
condition|)
block|{
name|Writer
name|w
init|=
name|writers
operator|.
name|get
argument_list|(
name|i
operator|-
name|skippedWriters
argument_list|)
decl_stmt|;
if|if
condition|(
name|requireMetadata
condition|)
block|{
name|assertNotNull
argument_list|(
name|w
operator|.
name|data
operator|.
name|get
argument_list|(
name|STRIPE_START_KEY
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|w
operator|.
name|data
operator|.
name|get
argument_list|(
name|STRIPE_END_KEY
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertNull
argument_list|(
name|w
operator|.
name|data
operator|.
name|get
argument_list|(
name|STRIPE_START_KEY
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|w
operator|.
name|data
operator|.
name|get
argument_list|(
name|STRIPE_END_KEY
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|kvs
operator|.
name|length
argument_list|,
name|w
operator|.
name|kvs
operator|.
name|size
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
name|kvs
operator|.
name|length
condition|;
operator|++
name|j
control|)
block|{
name|assertEquals
argument_list|(
name|kvs
index|[
name|j
index|]
argument_list|,
name|w
operator|.
name|kvs
operator|.
name|get
argument_list|(
name|j
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|assertFalse
argument_list|(
name|allFiles
argument_list|)
expr_stmt|;
operator|++
name|skippedWriters
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|void
name|verifyBoundaries
parameter_list|(
name|byte
index|[]
index|[]
name|boundaries
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|boundaries
operator|.
name|length
operator|-
literal|1
argument_list|,
name|writers
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|writers
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|assertArrayEquals
argument_list|(
literal|"i = "
operator|+
name|i
argument_list|,
name|boundaries
index|[
name|i
index|]
argument_list|,
name|writers
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|data
operator|.
name|get
argument_list|(
name|STRIPE_START_KEY
argument_list|)
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
literal|"i = "
operator|+
name|i
argument_list|,
name|boundaries
index|[
name|i
operator|+
literal|1
index|]
argument_list|,
name|writers
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|data
operator|.
name|get
argument_list|(
name|STRIPE_END_KEY
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|verifyKvs
parameter_list|(
name|KeyValue
index|[]
index|[]
name|kvss
parameter_list|,
name|boolean
name|allFiles
parameter_list|,
name|List
argument_list|<
name|Long
argument_list|>
name|boundaries
parameter_list|)
block|{
if|if
condition|(
name|allFiles
condition|)
block|{
name|assertEquals
argument_list|(
name|kvss
operator|.
name|length
argument_list|,
name|writers
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|int
name|skippedWriters
init|=
literal|0
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
name|kvss
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|KeyValue
index|[]
name|kvs
init|=
name|kvss
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|kvs
operator|!=
literal|null
condition|)
block|{
name|Writer
name|w
init|=
name|writers
operator|.
name|get
argument_list|(
name|i
operator|-
name|skippedWriters
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|kvs
operator|.
name|length
argument_list|,
name|w
operator|.
name|kvs
operator|.
name|size
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
name|kvs
operator|.
name|length
condition|;
operator|++
name|j
control|)
block|{
name|assertTrue
argument_list|(
name|kvs
index|[
name|j
index|]
operator|.
name|getTimestamp
argument_list|()
operator|>=
name|boundaries
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|kvs
index|[
name|j
index|]
operator|.
name|getTimestamp
argument_list|()
operator|<
name|boundaries
operator|.
name|get
argument_list|(
name|i
operator|+
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|kvs
index|[
name|j
index|]
argument_list|,
name|w
operator|.
name|kvs
operator|.
name|get
argument_list|(
name|j
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|assertFalse
argument_list|(
name|allFiles
argument_list|)
expr_stmt|;
operator|++
name|skippedWriters
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|List
argument_list|<
name|Writer
argument_list|>
name|getWriters
parameter_list|()
block|{
return|return
name|writers
return|;
block|}
block|}
specifier|public
specifier|static
class|class
name|Scanner
implements|implements
name|InternalScanner
block|{
specifier|private
specifier|final
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
name|kvs
decl_stmt|;
specifier|public
name|Scanner
parameter_list|(
name|KeyValue
modifier|...
name|kvs
parameter_list|)
block|{
name|this
operator|.
name|kvs
operator|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|kvs
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|next
parameter_list|(
name|List
argument_list|<
name|Cell
argument_list|>
name|results
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|kvs
operator|.
name|isEmpty
argument_list|()
condition|)
return|return
literal|false
return|;
name|results
operator|.
name|add
argument_list|(
name|kvs
operator|.
name|remove
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
return|return
operator|!
name|kvs
operator|.
name|isEmpty
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|next
parameter_list|(
name|List
argument_list|<
name|Cell
argument_list|>
name|result
parameter_list|,
name|ScannerContext
name|scannerContext
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|next
argument_list|(
name|result
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{     }
block|}
block|}
end_class

end_unit

