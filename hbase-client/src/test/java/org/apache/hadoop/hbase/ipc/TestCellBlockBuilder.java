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
name|ipc
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
name|Arrays
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
name|lang
operator|.
name|time
operator|.
name|StopWatch
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
name|commons
operator|.
name|logging
operator|.
name|impl
operator|.
name|Log4JLogger
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
name|conf
operator|.
name|Configuration
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
name|CellScanner
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
name|CellUtil
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
name|HBaseConfiguration
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
name|codec
operator|.
name|Codec
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
name|codec
operator|.
name|KeyValueCodec
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
name|SizedCellScanner
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
name|ClientTests
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
name|util
operator|.
name|Bytes
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
name|ClassSize
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
name|io
operator|.
name|compress
operator|.
name|CompressionCodec
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
name|io
operator|.
name|compress
operator|.
name|DefaultCodec
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
name|io
operator|.
name|compress
operator|.
name|GzipCodec
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|Level
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
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
name|ClientTests
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
name|TestCellBlockBuilder
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
name|TestCellBlockBuilder
operator|.
name|class
argument_list|)
decl_stmt|;
name|CellBlockBuilder
name|builder
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|before
parameter_list|()
block|{
name|this
operator|.
name|builder
operator|=
operator|new
name|CellBlockBuilder
argument_list|(
operator|new
name|Configuration
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBuildCellBlock
parameter_list|()
throws|throws
name|IOException
block|{
name|doBuildCellBlockUndoCellBlock
argument_list|(
name|this
operator|.
name|builder
argument_list|,
operator|new
name|KeyValueCodec
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|doBuildCellBlockUndoCellBlock
argument_list|(
name|this
operator|.
name|builder
argument_list|,
operator|new
name|KeyValueCodec
argument_list|()
argument_list|,
operator|new
name|DefaultCodec
argument_list|()
argument_list|)
expr_stmt|;
name|doBuildCellBlockUndoCellBlock
argument_list|(
name|this
operator|.
name|builder
argument_list|,
operator|new
name|KeyValueCodec
argument_list|()
argument_list|,
operator|new
name|GzipCodec
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|static
name|void
name|doBuildCellBlockUndoCellBlock
parameter_list|(
specifier|final
name|CellBlockBuilder
name|util
parameter_list|,
specifier|final
name|Codec
name|codec
parameter_list|,
specifier|final
name|CompressionCodec
name|compressor
parameter_list|)
throws|throws
name|IOException
block|{
name|doBuildCellBlockUndoCellBlock
argument_list|(
name|util
argument_list|,
name|codec
argument_list|,
name|compressor
argument_list|,
literal|10
argument_list|,
literal|1
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|static
name|void
name|doBuildCellBlockUndoCellBlock
parameter_list|(
specifier|final
name|CellBlockBuilder
name|util
parameter_list|,
specifier|final
name|Codec
name|codec
parameter_list|,
specifier|final
name|CompressionCodec
name|compressor
parameter_list|,
specifier|final
name|int
name|count
parameter_list|,
specifier|final
name|int
name|size
parameter_list|,
specifier|final
name|boolean
name|sized
parameter_list|)
throws|throws
name|IOException
block|{
name|Cell
index|[]
name|cells
init|=
name|getCells
argument_list|(
name|count
argument_list|,
name|size
argument_list|)
decl_stmt|;
name|CellScanner
name|cellScanner
init|=
name|sized
condition|?
name|getSizedCellScanner
argument_list|(
name|cells
argument_list|)
else|:
name|CellUtil
operator|.
name|createCellScanner
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|cells
argument_list|)
operator|.
name|iterator
argument_list|()
argument_list|)
decl_stmt|;
name|ByteBuffer
name|bb
init|=
name|util
operator|.
name|buildCellBlock
argument_list|(
name|codec
argument_list|,
name|compressor
argument_list|,
name|cellScanner
argument_list|)
decl_stmt|;
name|cellScanner
operator|=
name|util
operator|.
name|createCellScannerReusingBuffers
argument_list|(
name|codec
argument_list|,
name|compressor
argument_list|,
name|bb
argument_list|)
expr_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|cellScanner
operator|.
name|advance
argument_list|()
condition|)
block|{
name|i
operator|++
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|count
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
specifier|static
name|CellScanner
name|getSizedCellScanner
parameter_list|(
specifier|final
name|Cell
index|[]
name|cells
parameter_list|)
block|{
name|int
name|size
init|=
operator|-
literal|1
decl_stmt|;
for|for
control|(
name|Cell
name|cell
range|:
name|cells
control|)
block|{
name|size
operator|+=
name|CellUtil
operator|.
name|estimatedSerializedSizeOf
argument_list|(
name|cell
argument_list|)
expr_stmt|;
block|}
specifier|final
name|int
name|totalSize
init|=
name|ClassSize
operator|.
name|align
argument_list|(
name|size
argument_list|)
decl_stmt|;
specifier|final
name|CellScanner
name|cellScanner
init|=
name|CellUtil
operator|.
name|createCellScanner
argument_list|(
name|cells
argument_list|)
decl_stmt|;
return|return
operator|new
name|SizedCellScanner
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|long
name|heapSize
parameter_list|()
block|{
return|return
name|totalSize
return|;
block|}
annotation|@
name|Override
specifier|public
name|Cell
name|current
parameter_list|()
block|{
return|return
name|cellScanner
operator|.
name|current
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|advance
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|cellScanner
operator|.
name|advance
argument_list|()
return|;
block|}
block|}
return|;
block|}
specifier|static
name|Cell
index|[]
name|getCells
parameter_list|(
specifier|final
name|int
name|howMany
parameter_list|)
block|{
return|return
name|getCells
argument_list|(
name|howMany
argument_list|,
literal|1024
argument_list|)
return|;
block|}
specifier|static
name|Cell
index|[]
name|getCells
parameter_list|(
specifier|final
name|int
name|howMany
parameter_list|,
specifier|final
name|int
name|valueSize
parameter_list|)
block|{
name|Cell
index|[]
name|cells
init|=
operator|new
name|Cell
index|[
name|howMany
index|]
decl_stmt|;
name|byte
index|[]
name|value
init|=
operator|new
name|byte
index|[
name|valueSize
index|]
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
name|howMany
condition|;
name|i
operator|++
control|)
block|{
name|byte
index|[]
name|index
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|(
name|index
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f"
argument_list|)
argument_list|,
name|index
argument_list|,
name|value
argument_list|)
decl_stmt|;
name|cells
index|[
name|i
index|]
operator|=
name|kv
expr_stmt|;
block|}
return|return
name|cells
return|;
block|}
specifier|private
specifier|static
specifier|final
name|String
name|COUNT
init|=
literal|"--count="
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|SIZE
init|=
literal|"--size="
decl_stmt|;
comment|/**    * Prints usage and then exits w/ passed<code>errCode</code>    * @param errCode    */
specifier|private
specifier|static
name|void
name|usage
parameter_list|(
specifier|final
name|int
name|errCode
parameter_list|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Usage: IPCUtil [options]"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Micro-benchmarking how changed sizes and counts work with buffer resizing"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|" --count  Count of Cells"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|" --size   Size of Cell values"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Example: IPCUtil --count=1024 --size=1024"
argument_list|)
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
name|errCode
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|timerTests
parameter_list|(
specifier|final
name|CellBlockBuilder
name|util
parameter_list|,
specifier|final
name|int
name|count
parameter_list|,
specifier|final
name|int
name|size
parameter_list|,
specifier|final
name|Codec
name|codec
parameter_list|,
specifier|final
name|CompressionCodec
name|compressor
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|int
name|cycles
init|=
literal|1000
decl_stmt|;
name|StopWatch
name|timer
init|=
operator|new
name|StopWatch
argument_list|()
decl_stmt|;
name|timer
operator|.
name|start
argument_list|()
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
name|cycles
condition|;
name|i
operator|++
control|)
block|{
name|timerTest
argument_list|(
name|util
argument_list|,
name|timer
argument_list|,
name|count
argument_list|,
name|size
argument_list|,
name|codec
argument_list|,
name|compressor
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
name|timer
operator|.
name|stop
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Codec="
operator|+
name|codec
operator|+
literal|", compression="
operator|+
name|compressor
operator|+
literal|", sized="
operator|+
literal|false
operator|+
literal|", count="
operator|+
name|count
operator|+
literal|", size="
operator|+
name|size
operator|+
literal|", + took="
operator|+
name|timer
operator|.
name|getTime
argument_list|()
operator|+
literal|"ms"
argument_list|)
expr_stmt|;
name|timer
operator|.
name|reset
argument_list|()
expr_stmt|;
name|timer
operator|.
name|start
argument_list|()
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
name|cycles
condition|;
name|i
operator|++
control|)
block|{
name|timerTest
argument_list|(
name|util
argument_list|,
name|timer
argument_list|,
name|count
argument_list|,
name|size
argument_list|,
name|codec
argument_list|,
name|compressor
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
name|timer
operator|.
name|stop
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Codec="
operator|+
name|codec
operator|+
literal|", compression="
operator|+
name|compressor
operator|+
literal|", sized="
operator|+
literal|true
operator|+
literal|", count="
operator|+
name|count
operator|+
literal|", size="
operator|+
name|size
operator|+
literal|", + took="
operator|+
name|timer
operator|.
name|getTime
argument_list|()
operator|+
literal|"ms"
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|timerTest
parameter_list|(
specifier|final
name|CellBlockBuilder
name|util
parameter_list|,
specifier|final
name|StopWatch
name|timer
parameter_list|,
specifier|final
name|int
name|count
parameter_list|,
specifier|final
name|int
name|size
parameter_list|,
specifier|final
name|Codec
name|codec
parameter_list|,
specifier|final
name|CompressionCodec
name|compressor
parameter_list|,
specifier|final
name|boolean
name|sized
parameter_list|)
throws|throws
name|IOException
block|{
name|doBuildCellBlockUndoCellBlock
argument_list|(
name|util
argument_list|,
name|codec
argument_list|,
name|compressor
argument_list|,
name|count
argument_list|,
name|size
argument_list|,
name|sized
argument_list|)
expr_stmt|;
block|}
comment|/**    * For running a few tests of methods herein.    * @param args    * @throws IOException    */
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|count
init|=
literal|1024
decl_stmt|;
name|int
name|size
init|=
literal|10240
decl_stmt|;
for|for
control|(
name|String
name|arg
range|:
name|args
control|)
block|{
if|if
condition|(
name|arg
operator|.
name|startsWith
argument_list|(
name|COUNT
argument_list|)
condition|)
block|{
name|count
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|arg
operator|.
name|replace
argument_list|(
name|COUNT
argument_list|,
literal|""
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|arg
operator|.
name|startsWith
argument_list|(
name|SIZE
argument_list|)
condition|)
block|{
name|size
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|arg
operator|.
name|replace
argument_list|(
name|SIZE
argument_list|,
literal|""
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|usage
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
name|CellBlockBuilder
name|util
init|=
operator|new
name|CellBlockBuilder
argument_list|(
name|HBaseConfiguration
operator|.
name|create
argument_list|()
argument_list|)
decl_stmt|;
operator|(
operator|(
name|Log4JLogger
operator|)
name|CellBlockBuilder
operator|.
name|LOG
operator|)
operator|.
name|getLogger
argument_list|()
operator|.
name|setLevel
argument_list|(
name|Level
operator|.
name|ALL
argument_list|)
expr_stmt|;
name|timerTests
argument_list|(
name|util
argument_list|,
name|count
argument_list|,
name|size
argument_list|,
operator|new
name|KeyValueCodec
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|timerTests
argument_list|(
name|util
argument_list|,
name|count
argument_list|,
name|size
argument_list|,
operator|new
name|KeyValueCodec
argument_list|()
argument_list|,
operator|new
name|DefaultCodec
argument_list|()
argument_list|)
expr_stmt|;
name|timerTests
argument_list|(
name|util
argument_list|,
name|count
argument_list|,
name|size
argument_list|,
operator|new
name|KeyValueCodec
argument_list|()
argument_list|,
operator|new
name|GzipCodec
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

