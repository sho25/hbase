begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|io
operator|.
name|DataOutputBuffer
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
name|CompressionOutputStream
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
name|util
operator|.
name|NativeCodeLoader
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
name|util
operator|.
name|ReflectionUtils
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
name|java
operator|.
name|io
operator|.
name|BufferedOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutputStream
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

begin_class
annotation|@
name|Category
argument_list|(
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestCompressionTest
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestCompressionTest
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testExceptionCaching
parameter_list|()
block|{
comment|// This test will fail if you run the tests with LZO compression available.
try|try
block|{
name|CompressionTest
operator|.
name|testCompression
argument_list|(
name|Compression
operator|.
name|Algorithm
operator|.
name|LZO
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
comment|// always throws
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// there should be a 'cause'.
name|assertNotNull
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// this is testing the caching of the test results.
try|try
block|{
name|CompressionTest
operator|.
name|testCompression
argument_list|(
name|Compression
operator|.
name|Algorithm
operator|.
name|LZO
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
comment|// always throws
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// there should be NO cause because it's a direct exception not wrapped
name|assertNull
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|assertFalse
argument_list|(
name|CompressionTest
operator|.
name|testCompression
argument_list|(
literal|"LZO"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTestCompression
parameter_list|()
block|{
name|assertTrue
argument_list|(
name|CompressionTest
operator|.
name|testCompression
argument_list|(
literal|"NONE"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|CompressionTest
operator|.
name|testCompression
argument_list|(
literal|"GZ"
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|NativeCodeLoader
operator|.
name|isNativeCodeLoaded
argument_list|()
condition|)
block|{
name|nativeCodecTest
argument_list|(
literal|"LZO"
argument_list|,
literal|"lzo2"
argument_list|,
literal|"com.hadoop.compression.lzo.LzoCodec"
argument_list|)
expr_stmt|;
name|nativeCodecTest
argument_list|(
literal|"LZ4"
argument_list|,
literal|null
argument_list|,
literal|"org.apache.hadoop.io.compress.Lz4Codec"
argument_list|)
expr_stmt|;
name|nativeCodecTest
argument_list|(
literal|"SNAPPY"
argument_list|,
literal|"snappy"
argument_list|,
literal|"org.apache.hadoop.io.compress.SnappyCodec"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Hadoop nativelib is not available
name|LOG
operator|.
name|debug
argument_list|(
literal|"Native code not loaded"
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|CompressionTest
operator|.
name|testCompression
argument_list|(
literal|"LZO"
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|CompressionTest
operator|.
name|testCompression
argument_list|(
literal|"LZ4"
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|CompressionTest
operator|.
name|testCompression
argument_list|(
literal|"SNAPPY"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|boolean
name|isCompressionAvailable
parameter_list|(
name|String
name|codecClassName
parameter_list|)
block|{
try|try
block|{
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getContextClassLoader
argument_list|()
operator|.
name|loadClass
argument_list|(
name|codecClassName
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
block|}
comment|/**    * Verify CompressionTest.testCompression() on a native codec.    */
specifier|private
name|void
name|nativeCodecTest
parameter_list|(
name|String
name|codecName
parameter_list|,
name|String
name|libName
parameter_list|,
name|String
name|codecClassName
parameter_list|)
block|{
if|if
condition|(
name|isCompressionAvailable
argument_list|(
name|codecClassName
argument_list|)
condition|)
block|{
try|try
block|{
if|if
condition|(
name|libName
operator|!=
literal|null
condition|)
block|{
name|System
operator|.
name|loadLibrary
argument_list|(
name|libName
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|CompressionCodec
name|codec
init|=
operator|(
name|CompressionCodec
operator|)
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|conf
operator|.
name|getClassByName
argument_list|(
name|codecClassName
argument_list|)
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|DataOutputBuffer
name|compressedDataBuffer
init|=
operator|new
name|DataOutputBuffer
argument_list|()
decl_stmt|;
name|CompressionOutputStream
name|deflateFilter
init|=
name|codec
operator|.
name|createOutputStream
argument_list|(
name|compressedDataBuffer
argument_list|)
decl_stmt|;
name|byte
index|[]
name|data
init|=
operator|new
name|byte
index|[
literal|1024
index|]
decl_stmt|;
name|DataOutputStream
name|deflateOut
init|=
operator|new
name|DataOutputStream
argument_list|(
operator|new
name|BufferedOutputStream
argument_list|(
name|deflateFilter
argument_list|)
argument_list|)
decl_stmt|;
name|deflateOut
operator|.
name|write
argument_list|(
name|data
argument_list|,
literal|0
argument_list|,
name|data
operator|.
name|length
argument_list|)
expr_stmt|;
name|deflateOut
operator|.
name|flush
argument_list|()
expr_stmt|;
name|deflateFilter
operator|.
name|finish
argument_list|()
expr_stmt|;
comment|// Codec class, codec nativelib and Hadoop nativelib with codec JNIs are present
name|assertTrue
argument_list|(
name|CompressionTest
operator|.
name|testCompression
argument_list|(
name|codecName
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsatisfiedLinkError
name|e
parameter_list|)
block|{
comment|// Hadoop nativelib does not have codec JNIs.
comment|// cannot assert the codec here because the current logic of
comment|// CompressionTest checks only classloading, not the codec
comment|// usage.
name|LOG
operator|.
name|debug
argument_list|(
literal|"No JNI for codec '"
operator|+
name|codecName
operator|+
literal|"' "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|codecName
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|UnsatisfiedLinkError
name|e
parameter_list|)
block|{
comment|// nativelib is not available
name|LOG
operator|.
name|debug
argument_list|(
literal|"Native lib not available: "
operator|+
name|codecName
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|CompressionTest
operator|.
name|testCompression
argument_list|(
name|codecName
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// Compression Codec class is not available
name|LOG
operator|.
name|debug
argument_list|(
literal|"Codec class not available: "
operator|+
name|codecName
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|CompressionTest
operator|.
name|testCompression
argument_list|(
name|codecName
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

