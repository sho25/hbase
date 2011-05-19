begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|hadoop
operator|.
name|hbase
operator|.
name|io
operator|.
name|hfile
operator|.
name|Compression
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
specifier|public
class|class
name|TestCompressionTest
block|{
annotation|@
name|Test
specifier|public
name|void
name|testTestCompression
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
name|assertTrue
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
end_class

end_unit

