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
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|SecureRandom
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Random
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
name|math
operator|.
name|random
operator|.
name|RandomData
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
name|math
operator|.
name|random
operator|.
name|RandomDataImpl
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
name|fs
operator|.
name|FileSystem
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
name|classification
operator|.
name|InterfaceAudience
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
name|ImmutableBytesWritable
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
name|crypto
operator|.
name|Encryption
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
name|crypto
operator|.
name|KeyProviderForTesting
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
name|crypto
operator|.
name|aes
operator|.
name|AES
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
name|HFileWriterImpl
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
name|CacheConfig
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
name|io
operator|.
name|hfile
operator|.
name|HFileContext
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
name|HFileContextBuilder
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
name|HFileScanner
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

begin_comment
comment|/**  * This class runs performance benchmarks for {@link HFile}.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|TOOLS
argument_list|)
specifier|public
class|class
name|HFilePerformanceEvaluation
block|{
specifier|private
specifier|static
specifier|final
name|int
name|ROW_LENGTH
init|=
literal|10
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|ROW_COUNT
init|=
literal|1000000
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|RFILE_BLOCKSIZE
init|=
literal|8
operator|*
literal|1024
decl_stmt|;
specifier|private
specifier|static
name|StringBuilder
name|testSummary
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
comment|// Disable verbose INFO logging from org.apache.hadoop.io.compress.CodecPool
static|static
block|{
name|System
operator|.
name|setProperty
argument_list|(
literal|"org.apache.commons.logging.Log"
argument_list|,
literal|"org.apache.commons.logging.impl.SimpleLog"
argument_list|)
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
literal|"org.apache.commons.logging.simplelog.log.org.apache.hadoop.io.compress.CodecPool"
argument_list|,
literal|"WARN"
argument_list|)
expr_stmt|;
block|}
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|HFilePerformanceEvaluation
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|static
name|byte
index|[]
name|format
parameter_list|(
specifier|final
name|int
name|i
parameter_list|)
block|{
name|String
name|v
init|=
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
decl_stmt|;
return|return
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"0000000000"
operator|.
name|substring
argument_list|(
name|v
operator|.
name|length
argument_list|()
argument_list|)
operator|+
name|v
argument_list|)
return|;
block|}
specifier|static
name|ImmutableBytesWritable
name|format
parameter_list|(
specifier|final
name|int
name|i
parameter_list|,
name|ImmutableBytesWritable
name|w
parameter_list|)
block|{
name|w
operator|.
name|set
argument_list|(
name|format
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|w
return|;
block|}
specifier|static
name|Cell
name|createCell
parameter_list|(
specifier|final
name|int
name|i
parameter_list|)
block|{
return|return
name|createCell
argument_list|(
name|i
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|)
return|;
block|}
comment|/**    * HFile is Cell-based. It used to be byte arrays.  Doing this test, pass Cells. All Cells    * intentionally have same coordinates in all fields but row.    * @param i Integer to format as a row Key.    * @param value Value to use    * @return Created Cell.    */
specifier|static
name|Cell
name|createCell
parameter_list|(
specifier|final
name|int
name|i
parameter_list|,
specifier|final
name|byte
index|[]
name|value
parameter_list|)
block|{
return|return
name|createCell
argument_list|(
name|format
argument_list|(
name|i
argument_list|)
argument_list|,
name|value
argument_list|)
return|;
block|}
specifier|static
name|Cell
name|createCell
parameter_list|(
specifier|final
name|byte
index|[]
name|keyRow
parameter_list|)
block|{
return|return
name|CellUtil
operator|.
name|createCell
argument_list|(
name|keyRow
argument_list|)
return|;
block|}
specifier|static
name|Cell
name|createCell
parameter_list|(
specifier|final
name|byte
index|[]
name|keyRow
parameter_list|,
specifier|final
name|byte
index|[]
name|value
parameter_list|)
block|{
return|return
name|CellUtil
operator|.
name|createCell
argument_list|(
name|keyRow
argument_list|,
name|value
argument_list|)
return|;
block|}
comment|/**    * Add any supported codec or cipher to test the HFile read/write performance.     * Specify "none" to disable codec or cipher or both.      * @throws Exception    */
specifier|private
name|void
name|runBenchmarks
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
specifier|final
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
decl_stmt|;
specifier|final
name|Path
name|mf
init|=
name|fs
operator|.
name|makeQualified
argument_list|(
operator|new
name|Path
argument_list|(
literal|"performanceevaluation.mapfile"
argument_list|)
argument_list|)
decl_stmt|;
comment|// codec=none cipher=none
name|runWriteBenchmark
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|mf
argument_list|,
literal|"none"
argument_list|,
literal|"none"
argument_list|)
expr_stmt|;
name|runReadBenchmark
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|mf
argument_list|,
literal|"none"
argument_list|,
literal|"none"
argument_list|)
expr_stmt|;
comment|// codec=gz cipher=none
name|runWriteBenchmark
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|mf
argument_list|,
literal|"gz"
argument_list|,
literal|"none"
argument_list|)
expr_stmt|;
name|runReadBenchmark
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|mf
argument_list|,
literal|"gz"
argument_list|,
literal|"none"
argument_list|)
expr_stmt|;
comment|// Add configuration for AES cipher
specifier|final
name|Configuration
name|aesconf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|aesconf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|CRYPTO_KEYPROVIDER_CONF_KEY
argument_list|,
name|KeyProviderForTesting
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|aesconf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|CRYPTO_MASTERKEY_NAME_CONF_KEY
argument_list|,
literal|"hbase"
argument_list|)
expr_stmt|;
name|aesconf
operator|.
name|setInt
argument_list|(
literal|"hfile.format.version"
argument_list|,
literal|3
argument_list|)
expr_stmt|;
specifier|final
name|FileSystem
name|aesfs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|aesconf
argument_list|)
decl_stmt|;
specifier|final
name|Path
name|aesmf
init|=
name|aesfs
operator|.
name|makeQualified
argument_list|(
operator|new
name|Path
argument_list|(
literal|"performanceevaluation.aes.mapfile"
argument_list|)
argument_list|)
decl_stmt|;
comment|// codec=none cipher=aes
name|runWriteBenchmark
argument_list|(
name|aesconf
argument_list|,
name|aesfs
argument_list|,
name|aesmf
argument_list|,
literal|"none"
argument_list|,
literal|"aes"
argument_list|)
expr_stmt|;
name|runReadBenchmark
argument_list|(
name|aesconf
argument_list|,
name|aesfs
argument_list|,
name|aesmf
argument_list|,
literal|"none"
argument_list|,
literal|"aes"
argument_list|)
expr_stmt|;
comment|// codec=gz cipher=aes
name|runWriteBenchmark
argument_list|(
name|aesconf
argument_list|,
name|aesfs
argument_list|,
name|aesmf
argument_list|,
literal|"gz"
argument_list|,
literal|"aes"
argument_list|)
expr_stmt|;
name|runReadBenchmark
argument_list|(
name|aesconf
argument_list|,
name|aesfs
argument_list|,
name|aesmf
argument_list|,
literal|"gz"
argument_list|,
literal|"aes"
argument_list|)
expr_stmt|;
comment|// cleanup test files
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|mf
argument_list|)
condition|)
block|{
name|fs
operator|.
name|delete
argument_list|(
name|mf
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|aesfs
operator|.
name|exists
argument_list|(
name|aesmf
argument_list|)
condition|)
block|{
name|aesfs
operator|.
name|delete
argument_list|(
name|aesmf
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|// Print Result Summary
name|LOG
operator|.
name|info
argument_list|(
literal|"\n***************\n"
operator|+
literal|"Result Summary"
operator|+
literal|"\n***************\n"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|testSummary
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Write a test HFile with the given codec& cipher    * @param conf    * @param fs    * @param mf    * @param codec "none", "lzo", "gz", "snappy"    * @param cipher "none", "aes"    * @throws Exception    */
specifier|private
name|void
name|runWriteBenchmark
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|mf
parameter_list|,
name|String
name|codec
parameter_list|,
name|String
name|cipher
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|mf
argument_list|)
condition|)
block|{
name|fs
operator|.
name|delete
argument_list|(
name|mf
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
name|runBenchmark
argument_list|(
operator|new
name|SequentialWriteBenchmark
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|mf
argument_list|,
name|ROW_COUNT
argument_list|,
name|codec
argument_list|,
name|cipher
argument_list|)
argument_list|,
name|ROW_COUNT
argument_list|,
name|codec
argument_list|,
name|cipher
argument_list|)
expr_stmt|;
block|}
comment|/**    * Run all the read benchmarks for the test HFile     * @param conf    * @param fs    * @param mf    * @param codec "none", "lzo", "gz", "snappy"    * @param cipher "none", "aes"    */
specifier|private
name|void
name|runReadBenchmark
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|mf
parameter_list|,
specifier|final
name|String
name|codec
parameter_list|,
specifier|final
name|String
name|cipher
parameter_list|)
block|{
name|PerformanceEvaluationCommons
operator|.
name|concurrentReads
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|runBenchmark
argument_list|(
operator|new
name|UniformRandomSmallScan
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|mf
argument_list|,
name|ROW_COUNT
argument_list|)
argument_list|,
name|ROW_COUNT
argument_list|,
name|codec
argument_list|,
name|cipher
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|testSummary
operator|.
name|append
argument_list|(
literal|"UniformRandomSmallScan failed "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
name|PerformanceEvaluationCommons
operator|.
name|concurrentReads
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|runBenchmark
argument_list|(
operator|new
name|UniformRandomReadBenchmark
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|mf
argument_list|,
name|ROW_COUNT
argument_list|)
argument_list|,
name|ROW_COUNT
argument_list|,
name|codec
argument_list|,
name|cipher
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|testSummary
operator|.
name|append
argument_list|(
literal|"UniformRandomReadBenchmark failed "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
name|PerformanceEvaluationCommons
operator|.
name|concurrentReads
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|runBenchmark
argument_list|(
operator|new
name|GaussianRandomReadBenchmark
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|mf
argument_list|,
name|ROW_COUNT
argument_list|)
argument_list|,
name|ROW_COUNT
argument_list|,
name|codec
argument_list|,
name|cipher
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|testSummary
operator|.
name|append
argument_list|(
literal|"GaussianRandomReadBenchmark failed "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
name|PerformanceEvaluationCommons
operator|.
name|concurrentReads
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|runBenchmark
argument_list|(
operator|new
name|SequentialReadBenchmark
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|mf
argument_list|,
name|ROW_COUNT
argument_list|)
argument_list|,
name|ROW_COUNT
argument_list|,
name|codec
argument_list|,
name|cipher
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|testSummary
operator|.
name|append
argument_list|(
literal|"SequentialReadBenchmark failed "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|runBenchmark
parameter_list|(
name|RowOrientedBenchmark
name|benchmark
parameter_list|,
name|int
name|rowCount
parameter_list|,
name|String
name|codec
parameter_list|,
name|String
name|cipher
parameter_list|)
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Running "
operator|+
name|benchmark
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|" with codec["
operator|+
name|codec
operator|+
literal|"] "
operator|+
literal|"cipher["
operator|+
name|cipher
operator|+
literal|"] for "
operator|+
name|rowCount
operator|+
literal|" rows."
argument_list|)
expr_stmt|;
name|long
name|elapsedTime
init|=
name|benchmark
operator|.
name|run
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Running "
operator|+
name|benchmark
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|" with codec["
operator|+
name|codec
operator|+
literal|"] "
operator|+
literal|"cipher["
operator|+
name|cipher
operator|+
literal|"] for "
operator|+
name|rowCount
operator|+
literal|" rows took "
operator|+
name|elapsedTime
operator|+
literal|"ms."
argument_list|)
expr_stmt|;
comment|// Store results to print summary at the end
name|testSummary
operator|.
name|append
argument_list|(
literal|"Running "
argument_list|)
operator|.
name|append
argument_list|(
name|benchmark
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|" with codec["
argument_list|)
operator|.
name|append
argument_list|(
name|codec
argument_list|)
operator|.
name|append
argument_list|(
literal|"] cipher["
argument_list|)
operator|.
name|append
argument_list|(
name|cipher
argument_list|)
operator|.
name|append
argument_list|(
literal|"] for "
argument_list|)
operator|.
name|append
argument_list|(
name|rowCount
argument_list|)
operator|.
name|append
argument_list|(
literal|" rows took "
argument_list|)
operator|.
name|append
argument_list|(
name|elapsedTime
argument_list|)
operator|.
name|append
argument_list|(
literal|"ms."
argument_list|)
operator|.
name|append
argument_list|(
literal|"\n"
argument_list|)
expr_stmt|;
block|}
specifier|static
specifier|abstract
class|class
name|RowOrientedBenchmark
block|{
specifier|protected
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|protected
specifier|final
name|FileSystem
name|fs
decl_stmt|;
specifier|protected
specifier|final
name|Path
name|mf
decl_stmt|;
specifier|protected
specifier|final
name|int
name|totalRows
decl_stmt|;
specifier|protected
name|String
name|codec
init|=
literal|"none"
decl_stmt|;
specifier|protected
name|String
name|cipher
init|=
literal|"none"
decl_stmt|;
specifier|public
name|RowOrientedBenchmark
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|mf
parameter_list|,
name|int
name|totalRows
parameter_list|,
name|String
name|codec
parameter_list|,
name|String
name|cipher
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|fs
operator|=
name|fs
expr_stmt|;
name|this
operator|.
name|mf
operator|=
name|mf
expr_stmt|;
name|this
operator|.
name|totalRows
operator|=
name|totalRows
expr_stmt|;
name|this
operator|.
name|codec
operator|=
name|codec
expr_stmt|;
name|this
operator|.
name|cipher
operator|=
name|cipher
expr_stmt|;
block|}
specifier|public
name|RowOrientedBenchmark
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|mf
parameter_list|,
name|int
name|totalRows
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|fs
operator|=
name|fs
expr_stmt|;
name|this
operator|.
name|mf
operator|=
name|mf
expr_stmt|;
name|this
operator|.
name|totalRows
operator|=
name|totalRows
expr_stmt|;
block|}
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
comment|// do nothing
block|}
specifier|abstract
name|void
name|doRow
parameter_list|(
name|int
name|i
parameter_list|)
throws|throws
name|Exception
function_decl|;
specifier|protected
name|int
name|getReportingPeriod
parameter_list|()
block|{
return|return
name|this
operator|.
name|totalRows
operator|/
literal|10
return|;
block|}
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
comment|// do nothing
block|}
comment|/**      * Run benchmark      * @return elapsed time.      * @throws Exception      */
name|long
name|run
parameter_list|()
throws|throws
name|Exception
block|{
name|long
name|elapsedTime
decl_stmt|;
name|setUp
argument_list|()
expr_stmt|;
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
try|try
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|totalRows
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|>
literal|0
operator|&&
name|i
operator|%
name|getReportingPeriod
argument_list|()
operator|==
literal|0
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Processed "
operator|+
name|i
operator|+
literal|" rows."
argument_list|)
expr_stmt|;
block|}
name|doRow
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
name|elapsedTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startTime
expr_stmt|;
block|}
finally|finally
block|{
name|tearDown
argument_list|()
expr_stmt|;
block|}
return|return
name|elapsedTime
return|;
block|}
block|}
specifier|static
class|class
name|SequentialWriteBenchmark
extends|extends
name|RowOrientedBenchmark
block|{
specifier|protected
name|HFile
operator|.
name|Writer
name|writer
decl_stmt|;
specifier|private
name|Random
name|random
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
specifier|private
name|byte
index|[]
name|bytes
init|=
operator|new
name|byte
index|[
name|ROW_LENGTH
index|]
decl_stmt|;
specifier|public
name|SequentialWriteBenchmark
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|mf
parameter_list|,
name|int
name|totalRows
parameter_list|,
name|String
name|codec
parameter_list|,
name|String
name|cipher
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|mf
argument_list|,
name|totalRows
argument_list|,
name|codec
argument_list|,
name|cipher
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|HFileContextBuilder
name|builder
init|=
operator|new
name|HFileContextBuilder
argument_list|()
operator|.
name|withCompression
argument_list|(
name|HFileWriterImpl
operator|.
name|compressionByName
argument_list|(
name|codec
argument_list|)
argument_list|)
operator|.
name|withBlockSize
argument_list|(
name|RFILE_BLOCKSIZE
argument_list|)
decl_stmt|;
if|if
condition|(
name|cipher
operator|==
literal|"aes"
condition|)
block|{
name|byte
index|[]
name|cipherKey
init|=
operator|new
name|byte
index|[
name|AES
operator|.
name|KEY_LENGTH
index|]
decl_stmt|;
operator|new
name|SecureRandom
argument_list|()
operator|.
name|nextBytes
argument_list|(
name|cipherKey
argument_list|)
expr_stmt|;
name|builder
operator|.
name|withEncryptionContext
argument_list|(
name|Encryption
operator|.
name|newContext
argument_list|(
name|conf
argument_list|)
operator|.
name|setCipher
argument_list|(
name|Encryption
operator|.
name|getCipher
argument_list|(
name|conf
argument_list|,
name|cipher
argument_list|)
argument_list|)
operator|.
name|setKey
argument_list|(
name|cipherKey
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|!
literal|"none"
operator|.
name|equals
argument_list|(
name|cipher
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Cipher "
operator|+
name|cipher
operator|+
literal|" not supported."
argument_list|)
throw|;
block|}
name|HFileContext
name|hFileContext
init|=
name|builder
operator|.
name|build
argument_list|()
decl_stmt|;
name|writer
operator|=
name|HFile
operator|.
name|getWriterFactoryNoCache
argument_list|(
name|conf
argument_list|)
operator|.
name|withPath
argument_list|(
name|fs
argument_list|,
name|mf
argument_list|)
operator|.
name|withFileContext
argument_list|(
name|hFileContext
argument_list|)
operator|.
name|withComparator
argument_list|(
operator|new
name|KeyValue
operator|.
name|RawBytesComparator
argument_list|()
argument_list|)
operator|.
name|create
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
name|void
name|doRow
parameter_list|(
name|int
name|i
parameter_list|)
throws|throws
name|Exception
block|{
name|writer
operator|.
name|append
argument_list|(
name|createCell
argument_list|(
name|i
argument_list|,
name|generateValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|byte
index|[]
name|generateValue
parameter_list|()
block|{
name|random
operator|.
name|nextBytes
argument_list|(
name|bytes
argument_list|)
expr_stmt|;
return|return
name|bytes
return|;
block|}
annotation|@
name|Override
specifier|protected
name|int
name|getReportingPeriod
parameter_list|()
block|{
return|return
name|this
operator|.
name|totalRows
return|;
comment|// don't report progress
block|}
annotation|@
name|Override
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|static
specifier|abstract
class|class
name|ReadBenchmark
extends|extends
name|RowOrientedBenchmark
block|{
specifier|protected
name|HFile
operator|.
name|Reader
name|reader
decl_stmt|;
specifier|public
name|ReadBenchmark
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|mf
parameter_list|,
name|int
name|totalRows
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|mf
argument_list|,
name|totalRows
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|reader
operator|=
name|HFile
operator|.
name|createReader
argument_list|(
name|this
operator|.
name|fs
argument_list|,
name|this
operator|.
name|mf
argument_list|,
operator|new
name|CacheConfig
argument_list|(
name|this
operator|.
name|conf
argument_list|)
argument_list|,
name|this
operator|.
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|reader
operator|.
name|loadFileInfo
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|static
class|class
name|SequentialReadBenchmark
extends|extends
name|ReadBenchmark
block|{
specifier|private
name|HFileScanner
name|scanner
decl_stmt|;
specifier|public
name|SequentialReadBenchmark
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|mf
parameter_list|,
name|int
name|totalRows
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|mf
argument_list|,
name|totalRows
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
name|this
operator|.
name|scanner
operator|=
name|this
operator|.
name|reader
operator|.
name|getScanner
argument_list|(
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|this
operator|.
name|scanner
operator|.
name|seekTo
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
name|void
name|doRow
parameter_list|(
name|int
name|i
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|this
operator|.
name|scanner
operator|.
name|next
argument_list|()
condition|)
block|{
comment|// TODO: Fix. Make Scanner do Cells.
name|Cell
name|c
init|=
name|this
operator|.
name|scanner
operator|.
name|getKeyValue
argument_list|()
decl_stmt|;
name|PerformanceEvaluationCommons
operator|.
name|assertKey
argument_list|(
name|format
argument_list|(
name|i
operator|+
literal|1
argument_list|)
argument_list|,
name|c
argument_list|)
expr_stmt|;
name|PerformanceEvaluationCommons
operator|.
name|assertValueSize
argument_list|(
name|c
operator|.
name|getValueLength
argument_list|()
argument_list|,
name|ROW_LENGTH
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|int
name|getReportingPeriod
parameter_list|()
block|{
return|return
name|this
operator|.
name|totalRows
return|;
comment|// don't report progress
block|}
block|}
specifier|static
class|class
name|UniformRandomReadBenchmark
extends|extends
name|ReadBenchmark
block|{
specifier|private
name|Random
name|random
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
specifier|public
name|UniformRandomReadBenchmark
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|mf
parameter_list|,
name|int
name|totalRows
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|mf
argument_list|,
name|totalRows
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
name|void
name|doRow
parameter_list|(
name|int
name|i
parameter_list|)
throws|throws
name|Exception
block|{
name|HFileScanner
name|scanner
init|=
name|this
operator|.
name|reader
operator|.
name|getScanner
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|byte
index|[]
name|b
init|=
name|getRandomRow
argument_list|()
decl_stmt|;
if|if
condition|(
name|scanner
operator|.
name|seekTo
argument_list|(
name|createCell
argument_list|(
name|b
argument_list|)
argument_list|)
operator|<
literal|0
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Not able to seekTo "
operator|+
operator|new
name|String
argument_list|(
name|b
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// TODO: Fix scanner so it does Cells
name|Cell
name|c
init|=
name|scanner
operator|.
name|getKeyValue
argument_list|()
decl_stmt|;
name|PerformanceEvaluationCommons
operator|.
name|assertKey
argument_list|(
name|b
argument_list|,
name|c
argument_list|)
expr_stmt|;
name|PerformanceEvaluationCommons
operator|.
name|assertValueSize
argument_list|(
name|c
operator|.
name|getValueLength
argument_list|()
argument_list|,
name|ROW_LENGTH
argument_list|)
expr_stmt|;
block|}
specifier|private
name|byte
index|[]
name|getRandomRow
parameter_list|()
block|{
return|return
name|format
argument_list|(
name|random
operator|.
name|nextInt
argument_list|(
name|totalRows
argument_list|)
argument_list|)
return|;
block|}
block|}
specifier|static
class|class
name|UniformRandomSmallScan
extends|extends
name|ReadBenchmark
block|{
specifier|private
name|Random
name|random
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
specifier|public
name|UniformRandomSmallScan
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|mf
parameter_list|,
name|int
name|totalRows
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|mf
argument_list|,
name|totalRows
operator|/
literal|10
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
name|void
name|doRow
parameter_list|(
name|int
name|i
parameter_list|)
throws|throws
name|Exception
block|{
name|HFileScanner
name|scanner
init|=
name|this
operator|.
name|reader
operator|.
name|getScanner
argument_list|(
literal|false
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|byte
index|[]
name|b
init|=
name|getRandomRow
argument_list|()
decl_stmt|;
comment|// System.out.println("Random row: " + new String(b));
name|Cell
name|c
init|=
name|createCell
argument_list|(
name|b
argument_list|)
decl_stmt|;
if|if
condition|(
name|scanner
operator|.
name|seekTo
argument_list|(
name|c
argument_list|)
operator|!=
literal|0
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Nonexistent row: "
operator|+
operator|new
name|String
argument_list|(
name|b
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// TODO: HFileScanner doesn't do Cells yet. Temporary fix.
name|c
operator|=
name|scanner
operator|.
name|getKeyValue
argument_list|()
expr_stmt|;
comment|// System.out.println("Found row: " +
comment|//  new String(c.getRowArray(), c.getRowOffset(), c.getRowLength()));
name|PerformanceEvaluationCommons
operator|.
name|assertKey
argument_list|(
name|b
argument_list|,
name|c
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|ii
init|=
literal|0
init|;
name|ii
operator|<
literal|30
condition|;
name|ii
operator|++
control|)
block|{
if|if
condition|(
operator|!
name|scanner
operator|.
name|next
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"NOTHING FOLLOWS"
argument_list|)
expr_stmt|;
return|return;
block|}
name|c
operator|=
name|scanner
operator|.
name|getKeyValue
argument_list|()
expr_stmt|;
name|PerformanceEvaluationCommons
operator|.
name|assertValueSize
argument_list|(
name|c
operator|.
name|getValueLength
argument_list|()
argument_list|,
name|ROW_LENGTH
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|byte
index|[]
name|getRandomRow
parameter_list|()
block|{
return|return
name|format
argument_list|(
name|random
operator|.
name|nextInt
argument_list|(
name|totalRows
argument_list|)
argument_list|)
return|;
block|}
block|}
specifier|static
class|class
name|GaussianRandomReadBenchmark
extends|extends
name|ReadBenchmark
block|{
specifier|private
name|RandomData
name|randomData
init|=
operator|new
name|RandomDataImpl
argument_list|()
decl_stmt|;
specifier|public
name|GaussianRandomReadBenchmark
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|mf
parameter_list|,
name|int
name|totalRows
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|mf
argument_list|,
name|totalRows
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
name|void
name|doRow
parameter_list|(
name|int
name|i
parameter_list|)
throws|throws
name|Exception
block|{
name|HFileScanner
name|scanner
init|=
name|this
operator|.
name|reader
operator|.
name|getScanner
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|byte
index|[]
name|gaussianRandomRowBytes
init|=
name|getGaussianRandomRowBytes
argument_list|()
decl_stmt|;
name|scanner
operator|.
name|seekTo
argument_list|(
name|createCell
argument_list|(
name|gaussianRandomRowBytes
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|ii
init|=
literal|0
init|;
name|ii
operator|<
literal|30
condition|;
name|ii
operator|++
control|)
block|{
if|if
condition|(
operator|!
name|scanner
operator|.
name|next
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"NOTHING FOLLOWS"
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// TODO: Fix. Make scanner do Cells.
name|scanner
operator|.
name|getKeyValue
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|byte
index|[]
name|getGaussianRandomRowBytes
parameter_list|()
block|{
name|int
name|r
init|=
operator|(
name|int
operator|)
name|randomData
operator|.
name|nextGaussian
argument_list|(
operator|(
name|double
operator|)
name|totalRows
operator|/
literal|2.0
argument_list|,
operator|(
name|double
operator|)
name|totalRows
operator|/
literal|10.0
argument_list|)
decl_stmt|;
comment|// make sure r falls into [0,totalRows)
return|return
name|format
argument_list|(
name|Math
operator|.
name|min
argument_list|(
name|totalRows
argument_list|,
name|Math
operator|.
name|max
argument_list|(
name|r
argument_list|,
literal|0
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
block|}
comment|/**    * @param args    * @throws Exception    * @throws IOException    */
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
name|Exception
block|{
operator|new
name|HFilePerformanceEvaluation
argument_list|()
operator|.
name|runBenchmarks
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

