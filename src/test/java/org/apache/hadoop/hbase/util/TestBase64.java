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
name|java
operator|.
name|io
operator|.
name|UnsupportedEncodingException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
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
comment|/**  * Test order preservation characteristics of ordered Base64 dialect  */
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
name|TestBase64
extends|extends
name|TestCase
block|{
comment|// Note: uris is sorted. We need to prove that the ordered Base64
comment|// preserves that ordering
specifier|private
name|String
index|[]
name|uris
init|=
block|{
literal|"dns://dns.powerset.com/www.powerset.com"
block|,
literal|"dns:www.powerset.com"
block|,
literal|"file:///usr/bin/java"
block|,
literal|"filename"
block|,
literal|"ftp://one.two.three/index.html"
block|,
literal|"http://one.two.three/index.html"
block|,
literal|"https://one.two.three:9443/index.html"
block|,
literal|"r:dns://com.powerset.dns/www.powerset.com"
block|,
literal|"r:ftp://three.two.one/index.html"
block|,
literal|"r:http://three.two.one/index.html"
block|,
literal|"r:https://three.two.one:9443/index.html"
block|}
decl_stmt|;
comment|/**    * the test    * @throws UnsupportedEncodingException    */
specifier|public
name|void
name|testBase64
parameter_list|()
throws|throws
name|UnsupportedEncodingException
block|{
name|TreeMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|sorted
init|=
operator|new
name|TreeMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
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
name|uris
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|byte
index|[]
name|bytes
init|=
name|uris
index|[
name|i
index|]
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
decl_stmt|;
name|sorted
operator|.
name|put
argument_list|(
name|Base64
operator|.
name|encodeBytes
argument_list|(
name|bytes
argument_list|,
name|Base64
operator|.
name|ORDERED
argument_list|)
argument_list|,
name|uris
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|()
expr_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|e
range|:
name|sorted
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|assertTrue
argument_list|(
name|uris
index|[
name|i
operator|++
index|]
operator|.
name|compareTo
argument_list|(
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
operator|==
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

