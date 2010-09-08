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
name|*
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
name|List
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
name|HConstants
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
name|filter
operator|.
name|Filter
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
name|filter
operator|.
name|FilterBase
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
name|filter
operator|.
name|FilterList
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
name|filter
operator|.
name|PrefixFilter
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
name|Writable
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
name|WritableComparator
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
import|;
end_import

begin_class
specifier|public
class|class
name|TestHbaseObjectWritable
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
annotation|@
name|SuppressWarnings
argument_list|(
literal|"boxing"
argument_list|)
specifier|public
name|void
name|testReadObjectDataInputConfiguration
parameter_list|()
throws|throws
name|IOException
block|{
name|HBaseConfiguration
name|conf
init|=
operator|new
name|HBaseConfiguration
argument_list|()
decl_stmt|;
comment|// Do primitive type
specifier|final
name|int
name|COUNT
init|=
literal|101
decl_stmt|;
name|assertTrue
argument_list|(
name|doType
argument_list|(
name|conf
argument_list|,
name|COUNT
argument_list|,
name|int
operator|.
name|class
argument_list|)
operator|.
name|equals
argument_list|(
name|COUNT
argument_list|)
argument_list|)
expr_stmt|;
comment|// Do array
specifier|final
name|byte
index|[]
name|testing
init|=
literal|"testing"
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|byte
index|[]
name|result
init|=
operator|(
name|byte
index|[]
operator|)
name|doType
argument_list|(
name|conf
argument_list|,
name|testing
argument_list|,
name|testing
operator|.
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|WritableComparator
operator|.
name|compareBytes
argument_list|(
name|testing
argument_list|,
literal|0
argument_list|,
name|testing
operator|.
name|length
argument_list|,
name|result
argument_list|,
literal|0
argument_list|,
name|result
operator|.
name|length
argument_list|)
operator|==
literal|0
argument_list|)
expr_stmt|;
comment|// Do unsupported type.
name|boolean
name|exception
init|=
literal|false
decl_stmt|;
try|try
block|{
name|doType
argument_list|(
name|conf
argument_list|,
operator|new
name|File
argument_list|(
literal|"a"
argument_list|)
argument_list|,
name|File
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedOperationException
name|uoe
parameter_list|)
block|{
name|exception
operator|=
literal|true
expr_stmt|;
block|}
name|assertTrue
argument_list|(
name|exception
argument_list|)
expr_stmt|;
comment|// Try odd types
specifier|final
name|byte
name|A
init|=
literal|'A'
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
operator|new
name|byte
index|[
literal|1
index|]
decl_stmt|;
name|bytes
index|[
literal|0
index|]
operator|=
name|A
expr_stmt|;
name|Object
name|obj
init|=
name|doType
argument_list|(
name|conf
argument_list|,
name|bytes
argument_list|,
name|byte
index|[]
operator|.
expr|class
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
operator|(
operator|(
name|byte
index|[]
operator|)
name|obj
operator|)
index|[
literal|0
index|]
operator|==
name|A
argument_list|)
expr_stmt|;
comment|// Do 'known' Writable type.
name|obj
operator|=
name|doType
argument_list|(
name|conf
argument_list|,
operator|new
name|Text
argument_list|(
literal|""
argument_list|)
argument_list|,
name|Text
operator|.
name|class
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|obj
operator|instanceof
name|Text
argument_list|)
expr_stmt|;
comment|//List.class
name|List
argument_list|<
name|String
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|list
operator|.
name|add
argument_list|(
literal|"hello"
argument_list|)
expr_stmt|;
name|list
operator|.
name|add
argument_list|(
literal|"world"
argument_list|)
expr_stmt|;
name|list
operator|.
name|add
argument_list|(
literal|"universe"
argument_list|)
expr_stmt|;
name|obj
operator|=
name|doType
argument_list|(
name|conf
argument_list|,
name|list
argument_list|,
name|List
operator|.
name|class
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|obj
operator|instanceof
name|List
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertArrayEquals
argument_list|(
name|list
operator|.
name|toArray
argument_list|()
argument_list|,
operator|(
operator|(
name|List
operator|)
name|obj
operator|)
operator|.
name|toArray
argument_list|()
argument_list|)
expr_stmt|;
comment|//ArrayList.class
name|ArrayList
argument_list|<
name|String
argument_list|>
name|arr
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|arr
operator|.
name|add
argument_list|(
literal|"hello"
argument_list|)
expr_stmt|;
name|arr
operator|.
name|add
argument_list|(
literal|"world"
argument_list|)
expr_stmt|;
name|arr
operator|.
name|add
argument_list|(
literal|"universe"
argument_list|)
expr_stmt|;
name|obj
operator|=
name|doType
argument_list|(
name|conf
argument_list|,
name|arr
argument_list|,
name|ArrayList
operator|.
name|class
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|obj
operator|instanceof
name|ArrayList
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertArrayEquals
argument_list|(
name|list
operator|.
name|toArray
argument_list|()
argument_list|,
operator|(
operator|(
name|ArrayList
operator|)
name|obj
operator|)
operator|.
name|toArray
argument_list|()
argument_list|)
expr_stmt|;
comment|// Check that filters can be serialized
name|obj
operator|=
name|doType
argument_list|(
name|conf
argument_list|,
operator|new
name|PrefixFilter
argument_list|(
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|)
argument_list|,
name|PrefixFilter
operator|.
name|class
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|obj
operator|instanceof
name|PrefixFilter
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testCustomWritable
parameter_list|()
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
comment|// test proper serialization of un-encoded custom writables
name|CustomWritable
name|custom
init|=
operator|new
name|CustomWritable
argument_list|(
literal|"test phrase"
argument_list|)
decl_stmt|;
name|Object
name|obj
init|=
name|doType
argument_list|(
name|conf
argument_list|,
name|custom
argument_list|,
name|CustomWritable
operator|.
name|class
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|obj
operator|instanceof
name|Writable
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|obj
operator|instanceof
name|CustomWritable
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"test phrase"
argument_list|,
operator|(
operator|(
name|CustomWritable
operator|)
name|obj
operator|)
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
comment|// test proper serialization of a custom filter
name|CustomFilter
name|filt
init|=
operator|new
name|CustomFilter
argument_list|(
literal|"mykey"
argument_list|)
decl_stmt|;
name|FilterList
name|filtlist
init|=
operator|new
name|FilterList
argument_list|(
name|FilterList
operator|.
name|Operator
operator|.
name|MUST_PASS_ALL
argument_list|)
decl_stmt|;
name|filtlist
operator|.
name|addFilter
argument_list|(
name|filt
argument_list|)
expr_stmt|;
name|obj
operator|=
name|doType
argument_list|(
name|conf
argument_list|,
name|filtlist
argument_list|,
name|FilterList
operator|.
name|class
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|obj
operator|instanceof
name|FilterList
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
operator|(
operator|(
name|FilterList
operator|)
name|obj
operator|)
operator|.
name|getFilters
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
operator|(
operator|(
name|FilterList
operator|)
name|obj
operator|)
operator|.
name|getFilters
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Filter
name|child
init|=
operator|(
operator|(
name|FilterList
operator|)
name|obj
operator|)
operator|.
name|getFilters
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|child
operator|instanceof
name|CustomFilter
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"mykey"
argument_list|,
operator|(
operator|(
name|CustomFilter
operator|)
name|child
operator|)
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|Object
name|doType
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|Object
name|value
parameter_list|,
specifier|final
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
parameter_list|)
throws|throws
name|IOException
block|{
name|ByteArrayOutputStream
name|byteStream
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|DataOutputStream
name|out
init|=
operator|new
name|DataOutputStream
argument_list|(
name|byteStream
argument_list|)
decl_stmt|;
name|HbaseObjectWritable
operator|.
name|writeObject
argument_list|(
name|out
argument_list|,
name|value
argument_list|,
name|clazz
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|out
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
name|byteStream
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
name|Object
name|product
init|=
name|HbaseObjectWritable
operator|.
name|readObject
argument_list|(
name|dis
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|dis
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|product
return|;
block|}
specifier|public
specifier|static
class|class
name|CustomWritable
implements|implements
name|Writable
block|{
specifier|private
name|String
name|value
init|=
literal|null
decl_stmt|;
specifier|public
name|CustomWritable
parameter_list|()
block|{     }
specifier|public
name|CustomWritable
parameter_list|(
name|String
name|val
parameter_list|)
block|{
name|this
operator|.
name|value
operator|=
name|val
expr_stmt|;
block|}
specifier|public
name|String
name|getValue
parameter_list|()
block|{
return|return
name|value
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|Text
operator|.
name|writeString
argument_list|(
name|out
argument_list|,
name|this
operator|.
name|value
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|value
operator|=
name|Text
operator|.
name|readString
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|CustomFilter
extends|extends
name|FilterBase
block|{
specifier|private
name|String
name|key
init|=
literal|null
decl_stmt|;
specifier|public
name|CustomFilter
parameter_list|()
block|{     }
specifier|public
name|CustomFilter
parameter_list|(
name|String
name|key
parameter_list|)
block|{
name|this
operator|.
name|key
operator|=
name|key
expr_stmt|;
block|}
specifier|public
name|String
name|getKey
parameter_list|()
block|{
return|return
name|key
return|;
block|}
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|Text
operator|.
name|writeString
argument_list|(
name|out
argument_list|,
name|this
operator|.
name|key
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|key
operator|=
name|Text
operator|.
name|readString
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

