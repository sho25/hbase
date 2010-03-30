begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2009 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|util
operator|.
name|Bytes
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

begin_class
specifier|public
class|class
name|TestImmutableBytesWritable
extends|extends
name|TestCase
block|{
specifier|public
name|void
name|testComparison
parameter_list|()
throws|throws
name|Exception
block|{
name|runTests
argument_list|(
literal|"aa"
argument_list|,
literal|"b"
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|runTests
argument_list|(
literal|"aa"
argument_list|,
literal|"aa"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|runTests
argument_list|(
literal|"aa"
argument_list|,
literal|"ab"
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|runTests
argument_list|(
literal|"aa"
argument_list|,
literal|"aaa"
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|runTests
argument_list|(
literal|""
argument_list|,
literal|""
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|runTests
argument_list|(
literal|""
argument_list|,
literal|"a"
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|runTests
parameter_list|(
name|String
name|aStr
parameter_list|,
name|String
name|bStr
parameter_list|,
name|int
name|signum
parameter_list|)
throws|throws
name|Exception
block|{
name|ImmutableBytesWritable
name|a
init|=
operator|new
name|ImmutableBytesWritable
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|aStr
argument_list|)
argument_list|)
decl_stmt|;
name|ImmutableBytesWritable
name|b
init|=
operator|new
name|ImmutableBytesWritable
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|bStr
argument_list|)
argument_list|)
decl_stmt|;
name|doComparisonsOnObjects
argument_list|(
name|a
argument_list|,
name|b
argument_list|,
name|signum
argument_list|)
expr_stmt|;
name|doComparisonsOnRaw
argument_list|(
name|a
argument_list|,
name|b
argument_list|,
name|signum
argument_list|)
expr_stmt|;
block|}
specifier|private
name|int
name|signum
parameter_list|(
name|int
name|i
parameter_list|)
block|{
if|if
condition|(
name|i
operator|>
literal|0
condition|)
return|return
literal|1
return|;
if|if
condition|(
name|i
operator|==
literal|0
condition|)
return|return
literal|0
return|;
return|return
operator|-
literal|1
return|;
block|}
specifier|private
name|void
name|doComparisonsOnRaw
parameter_list|(
name|ImmutableBytesWritable
name|a
parameter_list|,
name|ImmutableBytesWritable
name|b
parameter_list|,
name|int
name|expectedSignum
parameter_list|)
throws|throws
name|IOException
block|{
name|ImmutableBytesWritable
operator|.
name|Comparator
name|comparator
init|=
operator|new
name|ImmutableBytesWritable
operator|.
name|Comparator
argument_list|()
decl_stmt|;
name|ByteArrayOutputStream
name|baosA
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|ByteArrayOutputStream
name|baosB
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|a
operator|.
name|write
argument_list|(
operator|new
name|DataOutputStream
argument_list|(
name|baosA
argument_list|)
argument_list|)
expr_stmt|;
name|b
operator|.
name|write
argument_list|(
operator|new
name|DataOutputStream
argument_list|(
name|baosB
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Comparing "
operator|+
name|a
operator|+
literal|" and "
operator|+
name|b
operator|+
literal|" as raw"
argument_list|,
name|signum
argument_list|(
name|comparator
operator|.
name|compare
argument_list|(
name|baosA
operator|.
name|toByteArray
argument_list|()
argument_list|,
literal|0
argument_list|,
name|baosA
operator|.
name|size
argument_list|()
argument_list|,
name|baosB
operator|.
name|toByteArray
argument_list|()
argument_list|,
literal|0
argument_list|,
name|baosB
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
argument_list|,
name|expectedSignum
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Comparing "
operator|+
name|a
operator|+
literal|" and "
operator|+
name|b
operator|+
literal|" as raw (inverse)"
argument_list|,
operator|-
name|signum
argument_list|(
name|comparator
operator|.
name|compare
argument_list|(
name|baosB
operator|.
name|toByteArray
argument_list|()
argument_list|,
literal|0
argument_list|,
name|baosB
operator|.
name|size
argument_list|()
argument_list|,
name|baosA
operator|.
name|toByteArray
argument_list|()
argument_list|,
literal|0
argument_list|,
name|baosA
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
argument_list|,
name|expectedSignum
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|doComparisonsOnObjects
parameter_list|(
name|ImmutableBytesWritable
name|a
parameter_list|,
name|ImmutableBytesWritable
name|b
parameter_list|,
name|int
name|expectedSignum
parameter_list|)
block|{
name|ImmutableBytesWritable
operator|.
name|Comparator
name|comparator
init|=
operator|new
name|ImmutableBytesWritable
operator|.
name|Comparator
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Comparing "
operator|+
name|a
operator|+
literal|" and "
operator|+
name|b
operator|+
literal|" as objects"
argument_list|,
name|signum
argument_list|(
name|comparator
operator|.
name|compare
argument_list|(
name|a
argument_list|,
name|b
argument_list|)
argument_list|)
argument_list|,
name|expectedSignum
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Comparing "
operator|+
name|a
operator|+
literal|" and "
operator|+
name|b
operator|+
literal|" as objects (inverse)"
argument_list|,
operator|-
name|signum
argument_list|(
name|comparator
operator|.
name|compare
argument_list|(
name|b
argument_list|,
name|a
argument_list|)
argument_list|)
argument_list|,
name|expectedSignum
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

