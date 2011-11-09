begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|junit
operator|.
name|framework
operator|.
name|TestCase
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
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_comment
comment|/**  * Tests url transformations  */
end_comment

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
name|TestKeying
extends|extends
name|TestCase
block|{
annotation|@
name|Override
specifier|protected
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
block|}
annotation|@
name|Override
specifier|protected
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|tearDown
argument_list|()
expr_stmt|;
block|}
comment|/**    * Test url transformations    * @throws Exception    */
specifier|public
name|void
name|testURI
parameter_list|()
throws|throws
name|Exception
block|{
name|checkTransform
argument_list|(
literal|"http://abc:bcd@www.example.com/index.html"
operator|+
literal|"?query=something#middle"
argument_list|)
expr_stmt|;
name|checkTransform
argument_list|(
literal|"file:///usr/bin/java"
argument_list|)
expr_stmt|;
name|checkTransform
argument_list|(
literal|"dns:www.powerset.com"
argument_list|)
expr_stmt|;
name|checkTransform
argument_list|(
literal|"dns://dns.powerset.com/www.powerset.com"
argument_list|)
expr_stmt|;
name|checkTransform
argument_list|(
literal|"http://one.two.three/index.html"
argument_list|)
expr_stmt|;
name|checkTransform
argument_list|(
literal|"https://one.two.three:9443/index.html"
argument_list|)
expr_stmt|;
name|checkTransform
argument_list|(
literal|"ftp://one.two.three/index.html"
argument_list|)
expr_stmt|;
name|checkTransform
argument_list|(
literal|"filename"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|checkTransform
parameter_list|(
specifier|final
name|String
name|u
parameter_list|)
block|{
name|String
name|k
init|=
name|Keying
operator|.
name|createKey
argument_list|(
name|u
argument_list|)
decl_stmt|;
name|String
name|uri
init|=
name|Keying
operator|.
name|keyToUri
argument_list|(
name|k
argument_list|)
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Original url "
operator|+
name|u
operator|+
literal|", Transformed url "
operator|+
name|k
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|u
argument_list|,
name|uri
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

