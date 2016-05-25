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
operator|.
name|util
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
name|util
operator|.
name|Locale
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
name|StringUtils
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
name|classification
operator|.
name|InterfaceStability
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
name|CellComparator
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
name|DoNotRetryIOException
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
name|HBaseInterfaceAudience
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
name|compress
operator|.
name|Compression
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
name|io
operator|.
name|compress
operator|.
name|Compressor
import|;
end_import

begin_comment
comment|/**  * Compression validation test.  Checks compression is working.  Be sure to run  * on every node in your cluster.  */
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
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|CompressionTest
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
name|CompressionTest
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|boolean
name|testCompression
parameter_list|(
name|String
name|codec
parameter_list|)
block|{
name|codec
operator|=
name|codec
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
expr_stmt|;
name|Compression
operator|.
name|Algorithm
name|a
decl_stmt|;
try|try
block|{
name|a
operator|=
name|Compression
operator|.
name|getCompressionAlgorithmByName
argument_list|(
name|codec
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Codec type: "
operator|+
name|codec
operator|+
literal|" is not known"
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
try|try
block|{
name|testCompression
argument_list|(
name|a
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ignored
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Can't instantiate codec: "
operator|+
name|codec
argument_list|,
name|ignored
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
specifier|private
specifier|final
specifier|static
name|Boolean
index|[]
name|compressionTestResults
init|=
operator|new
name|Boolean
index|[
name|Compression
operator|.
name|Algorithm
operator|.
name|values
argument_list|()
operator|.
name|length
index|]
decl_stmt|;
static|static
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
name|compressionTestResults
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|compressionTestResults
index|[
name|i
index|]
operator|=
literal|null
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|void
name|testCompression
parameter_list|(
name|Compression
operator|.
name|Algorithm
name|algo
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|compressionTestResults
index|[
name|algo
operator|.
name|ordinal
argument_list|()
index|]
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|compressionTestResults
index|[
name|algo
operator|.
name|ordinal
argument_list|()
index|]
condition|)
block|{
return|return ;
comment|// already passed test, dont do it again.
block|}
else|else
block|{
comment|// failed.
throw|throw
operator|new
name|DoNotRetryIOException
argument_list|(
literal|"Compression algorithm '"
operator|+
name|algo
operator|.
name|getName
argument_list|()
operator|+
literal|"'"
operator|+
literal|" previously failed test."
argument_list|)
throw|;
block|}
block|}
try|try
block|{
name|Compressor
name|c
init|=
name|algo
operator|.
name|getCompressor
argument_list|()
decl_stmt|;
name|algo
operator|.
name|returnCompressor
argument_list|(
name|c
argument_list|)
expr_stmt|;
name|compressionTestResults
index|[
name|algo
operator|.
name|ordinal
argument_list|()
index|]
operator|=
literal|true
expr_stmt|;
comment|// passes
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|compressionTestResults
index|[
name|algo
operator|.
name|ordinal
argument_list|()
index|]
operator|=
literal|false
expr_stmt|;
comment|// failure
throw|throw
operator|new
name|DoNotRetryIOException
argument_list|(
name|t
argument_list|)
throw|;
block|}
block|}
specifier|protected
specifier|static
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
literal|".hfile-comp-test"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|void
name|usage
parameter_list|()
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Usage: CompressionTest<path> "
operator|+
name|StringUtils
operator|.
name|join
argument_list|(
name|Compression
operator|.
name|Algorithm
operator|.
name|values
argument_list|()
argument_list|,
literal|"|"
argument_list|)
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
operator|+
literal|"\n"
operator|+
literal|"For example:\n"
operator|+
literal|"  hbase "
operator|+
name|CompressionTest
operator|.
name|class
operator|+
literal|" file:///tmp/testfile gz\n"
argument_list|)
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|doSmokeTest
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|,
name|String
name|codec
parameter_list|)
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|HFileContext
name|context
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
name|build
argument_list|()
decl_stmt|;
name|HFile
operator|.
name|Writer
name|writer
init|=
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
name|path
argument_list|)
operator|.
name|withFileContext
argument_list|(
name|context
argument_list|)
operator|.
name|create
argument_list|()
decl_stmt|;
comment|// Write any-old Cell...
specifier|final
name|byte
index|[]
name|rowKey
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"compressiontestkey"
argument_list|)
decl_stmt|;
name|Cell
name|c
init|=
name|CellUtil
operator|.
name|createCell
argument_list|(
name|rowKey
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"compressiontestval"
argument_list|)
argument_list|)
decl_stmt|;
name|writer
operator|.
name|append
argument_list|(
name|c
argument_list|)
expr_stmt|;
name|writer
operator|.
name|appendFileInfo
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"compressioninfokey"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"compressioninfoval"
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
name|Cell
name|cc
init|=
literal|null
decl_stmt|;
name|HFile
operator|.
name|Reader
name|reader
init|=
name|HFile
operator|.
name|createReader
argument_list|(
name|fs
argument_list|,
name|path
argument_list|,
operator|new
name|CacheConfig
argument_list|(
name|conf
argument_list|)
argument_list|,
name|conf
argument_list|)
decl_stmt|;
try|try
block|{
name|reader
operator|.
name|loadFileInfo
argument_list|()
expr_stmt|;
name|HFileScanner
name|scanner
init|=
name|reader
operator|.
name|getScanner
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|scanner
operator|.
name|seekTo
argument_list|()
expr_stmt|;
comment|// position to the start of file
comment|// Scanner does not do Cells yet. Do below for now till fixed.
name|cc
operator|=
name|scanner
operator|.
name|getCell
argument_list|()
expr_stmt|;
if|if
condition|(
name|CellComparator
operator|.
name|COMPARATOR
operator|.
name|compareRows
argument_list|(
name|c
argument_list|,
name|cc
argument_list|)
operator|!=
literal|0
condition|)
block|{
throw|throw
operator|new
name|Exception
argument_list|(
literal|"Read back incorrect result: "
operator|+
name|c
operator|.
name|toString
argument_list|()
operator|+
literal|" vs "
operator|+
name|cc
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
block|}
finally|finally
block|{
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
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
if|if
condition|(
name|args
operator|.
name|length
operator|!=
literal|2
condition|)
block|{
name|usage
argument_list|()
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
name|args
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|path
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|path
argument_list|)
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"The specified path exists, aborting!"
argument_list|)
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|doSmokeTest
argument_list|(
name|fs
argument_list|,
name|path
argument_list|,
name|args
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|fs
operator|.
name|delete
argument_list|(
name|path
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"SUCCESS"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

