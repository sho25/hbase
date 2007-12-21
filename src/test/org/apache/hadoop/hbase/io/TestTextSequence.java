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
name|io
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInputStream
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|HBaseTestCase
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
name|Text
import|;
end_import

begin_class
specifier|public
class|class
name|TestTextSequence
extends|extends
name|HBaseTestCase
block|{
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
comment|/**    * Test compares of TextSequences and of TextSequence to Text.    * @throws Exception    */
specifier|public
name|void
name|testCompare
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|Text
name|a
init|=
operator|new
name|Text
argument_list|(
literal|"abcdef"
argument_list|)
decl_stmt|;
specifier|final
name|Text
name|b
init|=
operator|new
name|Text
argument_list|(
literal|"defghi"
argument_list|)
decl_stmt|;
name|TextSequence
name|as
init|=
operator|new
name|TextSequence
argument_list|(
name|a
argument_list|,
literal|3
argument_list|)
decl_stmt|;
name|TextSequence
name|bs
init|=
operator|new
name|TextSequence
argument_list|(
name|b
argument_list|,
literal|0
argument_list|,
literal|3
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|as
operator|.
name|compareTo
argument_list|(
name|bs
argument_list|)
operator|==
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|as
operator|.
name|equals
argument_list|(
name|bs
argument_list|)
argument_list|)
expr_stmt|;
comment|// Test where one is a Text and other is a TextSequence
specifier|final
name|Text
name|family
init|=
operator|new
name|Text
argument_list|(
literal|"abc:"
argument_list|)
decl_stmt|;
specifier|final
name|Text
name|column
init|=
operator|new
name|Text
argument_list|(
name|family
operator|.
name|toString
argument_list|()
operator|+
literal|"qualifier"
argument_list|)
decl_stmt|;
specifier|final
name|TextSequence
name|ts
init|=
operator|new
name|TextSequence
argument_list|(
name|column
argument_list|,
literal|0
argument_list|,
name|family
operator|.
name|getLength
argument_list|()
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|ts
operator|.
name|compareTo
argument_list|(
name|family
argument_list|)
operator|==
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|ts
operator|.
name|equals
argument_list|(
name|family
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testSerialize
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|Text
name|t
init|=
operator|new
name|Text
argument_list|(
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|TextSequence
name|ts
init|=
operator|new
name|TextSequence
argument_list|(
name|t
argument_list|,
literal|1
argument_list|,
literal|3
argument_list|)
decl_stmt|;
name|ByteArrayOutputStream
name|baos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|DataOutputStream
name|dao
init|=
operator|new
name|DataOutputStream
argument_list|(
name|baos
argument_list|)
decl_stmt|;
name|ts
operator|.
name|write
argument_list|(
name|dao
argument_list|)
expr_stmt|;
name|dao
operator|.
name|close
argument_list|()
expr_stmt|;
name|ByteArrayInputStream
name|bais
init|=
operator|new
name|ByteArrayInputStream
argument_list|(
name|baos
operator|.
name|toByteArray
argument_list|()
argument_list|)
decl_stmt|;
name|DataInputStream
name|dis
init|=
operator|new
name|DataInputStream
argument_list|(
name|bais
argument_list|)
decl_stmt|;
name|TextSequence
name|deserializeTs
init|=
operator|new
name|TextSequence
argument_list|()
decl_stmt|;
name|deserializeTs
operator|.
name|readFields
argument_list|(
name|dis
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|ts
operator|.
name|equals
argument_list|(
name|deserializeTs
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

