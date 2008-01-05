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
name|io
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
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
name|lang
operator|.
name|reflect
operator|.
name|Array
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|Configurable
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
name|conf
operator|.
name|Configured
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
name|HColumnDescriptor
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
name|HMsg
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
name|HRegionInfo
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
name|HServerAddress
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
name|HServerInfo
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
name|HTableDescriptor
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
name|RowFilterInterface
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
name|RowFilterSet
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
name|MapWritable
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
name|ObjectWritable
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
name|WritableFactories
import|;
end_import

begin_comment
comment|/**   * This is a customized version of the polymorphic hadoop  * {@link ObjectWritable}.  It removes UTF8 (HADOOP-414).  * Using {@link Text} intead of UTF-8 saves ~2% CPU between reading and writing  * objects running a short sequentialWrite Performance Evaluation test just in  * ObjectWritable alone; more when we're doing randomRead-ing.  Other  * optimizations include our passing codes for classes instead of the  * actual class names themselves.  This makes it so this class needs amendment  * if non-Writable classes are introduced -- if passed a Writable for which we  * have no code, we just do the old-school passing of the class name, etc. --  * but passing codes the  savings are large particularly when cell  * data is small (If< a couple of kilobytes, the encoding/decoding of class  * name and reflection to instantiate class was costing in excess of the cell  * handling).  */
end_comment

begin_class
specifier|public
class|class
name|HbaseObjectWritable
implements|implements
name|Writable
implements|,
name|Configurable
block|{
specifier|protected
specifier|final
specifier|static
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|HbaseObjectWritable
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// Here we maintain two static maps of classes to code and vice versa.
comment|// Add new classes+codes as wanted or figure way to auto-generate these
comment|// maps from the HMasterInterface.
specifier|static
specifier|final
name|Map
argument_list|<
name|Byte
argument_list|,
name|Class
argument_list|<
name|?
argument_list|>
argument_list|>
name|CODE_TO_CLASS
init|=
operator|new
name|HashMap
argument_list|<
name|Byte
argument_list|,
name|Class
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
specifier|static
specifier|final
name|Map
argument_list|<
name|Class
argument_list|<
name|?
argument_list|>
argument_list|,
name|Byte
argument_list|>
name|CLASS_TO_CODE
init|=
operator|new
name|HashMap
argument_list|<
name|Class
argument_list|<
name|?
argument_list|>
argument_list|,
name|Byte
argument_list|>
argument_list|()
decl_stmt|;
comment|// Special code that means 'not-encoded'; in this case we do old school
comment|// sending of the class name using reflection, etc.
specifier|private
specifier|static
specifier|final
name|byte
name|NOT_ENCODED
init|=
literal|0
decl_stmt|;
static|static
block|{
name|byte
name|code
init|=
name|NOT_ENCODED
operator|+
literal|1
decl_stmt|;
comment|// Primitive types.
name|addToMap
argument_list|(
name|Boolean
operator|.
name|TYPE
argument_list|,
name|code
operator|++
argument_list|)
expr_stmt|;
name|addToMap
argument_list|(
name|Byte
operator|.
name|TYPE
argument_list|,
name|code
operator|++
argument_list|)
expr_stmt|;
name|addToMap
argument_list|(
name|Character
operator|.
name|TYPE
argument_list|,
name|code
operator|++
argument_list|)
expr_stmt|;
name|addToMap
argument_list|(
name|Short
operator|.
name|TYPE
argument_list|,
name|code
operator|++
argument_list|)
expr_stmt|;
name|addToMap
argument_list|(
name|Integer
operator|.
name|TYPE
argument_list|,
name|code
operator|++
argument_list|)
expr_stmt|;
name|addToMap
argument_list|(
name|Long
operator|.
name|TYPE
argument_list|,
name|code
operator|++
argument_list|)
expr_stmt|;
name|addToMap
argument_list|(
name|Float
operator|.
name|TYPE
argument_list|,
name|code
operator|++
argument_list|)
expr_stmt|;
name|addToMap
argument_list|(
name|Double
operator|.
name|TYPE
argument_list|,
name|code
operator|++
argument_list|)
expr_stmt|;
name|addToMap
argument_list|(
name|Void
operator|.
name|TYPE
argument_list|,
name|code
operator|++
argument_list|)
expr_stmt|;
comment|// Other java types
name|addToMap
argument_list|(
name|String
operator|.
name|class
argument_list|,
name|code
operator|++
argument_list|)
expr_stmt|;
name|addToMap
argument_list|(
name|byte
index|[]
operator|.
expr|class
argument_list|,
name|code
operator|++
argument_list|)
expr_stmt|;
name|addToMap
argument_list|(
name|byte
index|[]
index|[]
operator|.
expr|class
argument_list|,
name|code
operator|++
argument_list|)
expr_stmt|;
comment|// Hadoop types
name|addToMap
argument_list|(
name|Text
operator|.
name|class
argument_list|,
name|code
operator|++
argument_list|)
expr_stmt|;
name|addToMap
argument_list|(
name|Writable
operator|.
name|class
argument_list|,
name|code
operator|++
argument_list|)
expr_stmt|;
name|addToMap
argument_list|(
name|MapWritable
operator|.
name|class
argument_list|,
name|code
operator|++
argument_list|)
expr_stmt|;
name|addToMap
argument_list|(
name|NullInstance
operator|.
name|class
argument_list|,
name|code
operator|++
argument_list|)
expr_stmt|;
try|try
block|{
name|addToMap
argument_list|(
name|Class
operator|.
name|forName
argument_list|(
literal|"[Lorg.apache.hadoop.io.Text;"
argument_list|)
argument_list|,
name|code
operator|++
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
comment|// Hbase types
name|addToMap
argument_list|(
name|HServerInfo
operator|.
name|class
argument_list|,
name|code
operator|++
argument_list|)
expr_stmt|;
name|addToMap
argument_list|(
name|HMsg
operator|.
name|class
argument_list|,
name|code
operator|++
argument_list|)
expr_stmt|;
name|addToMap
argument_list|(
name|HTableDescriptor
operator|.
name|class
argument_list|,
name|code
operator|++
argument_list|)
expr_stmt|;
name|addToMap
argument_list|(
name|HColumnDescriptor
operator|.
name|class
argument_list|,
name|code
operator|++
argument_list|)
expr_stmt|;
name|addToMap
argument_list|(
name|RowFilterInterface
operator|.
name|class
argument_list|,
name|code
operator|++
argument_list|)
expr_stmt|;
name|addToMap
argument_list|(
name|RowFilterSet
operator|.
name|class
argument_list|,
name|code
operator|++
argument_list|)
expr_stmt|;
name|addToMap
argument_list|(
name|HRegionInfo
operator|.
name|class
argument_list|,
name|code
operator|++
argument_list|)
expr_stmt|;
name|addToMap
argument_list|(
name|BatchUpdate
operator|.
name|class
argument_list|,
name|code
operator|++
argument_list|)
expr_stmt|;
name|addToMap
argument_list|(
name|HServerAddress
operator|.
name|class
argument_list|,
name|code
operator|++
argument_list|)
expr_stmt|;
name|addToMap
argument_list|(
name|HRegionInfo
operator|.
name|class
argument_list|,
name|code
operator|++
argument_list|)
expr_stmt|;
try|try
block|{
name|addToMap
argument_list|(
name|Class
operator|.
name|forName
argument_list|(
literal|"[Lorg.apache.hadoop.hbase.HMsg;"
argument_list|)
argument_list|,
name|code
operator|++
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|Class
argument_list|<
name|?
argument_list|>
name|declaredClass
decl_stmt|;
specifier|private
name|Object
name|instance
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|public
name|HbaseObjectWritable
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|HbaseObjectWritable
parameter_list|(
name|Object
name|instance
parameter_list|)
block|{
name|set
argument_list|(
name|instance
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HbaseObjectWritable
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|declaredClass
parameter_list|,
name|Object
name|instance
parameter_list|)
block|{
name|this
operator|.
name|declaredClass
operator|=
name|declaredClass
expr_stmt|;
name|this
operator|.
name|instance
operator|=
name|instance
expr_stmt|;
block|}
comment|/** Return the instance, or null if none. */
specifier|public
name|Object
name|get
parameter_list|()
block|{
return|return
name|instance
return|;
block|}
comment|/** Return the class this is meant to be. */
specifier|public
name|Class
argument_list|<
name|?
argument_list|>
name|getDeclaredClass
parameter_list|()
block|{
return|return
name|declaredClass
return|;
block|}
comment|/** Reset the instance. */
specifier|public
name|void
name|set
parameter_list|(
name|Object
name|instance
parameter_list|)
block|{
name|this
operator|.
name|declaredClass
operator|=
name|instance
operator|.
name|getClass
argument_list|()
expr_stmt|;
name|this
operator|.
name|instance
operator|=
name|instance
expr_stmt|;
block|}
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"OW[class="
operator|+
name|declaredClass
operator|+
literal|",value="
operator|+
name|instance
operator|+
literal|"]"
return|;
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
name|readObject
argument_list|(
name|in
argument_list|,
name|this
argument_list|,
name|this
operator|.
name|conf
argument_list|)
expr_stmt|;
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
name|writeObject
argument_list|(
name|out
argument_list|,
name|instance
argument_list|,
name|declaredClass
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
class|class
name|NullInstance
extends|extends
name|Configured
implements|implements
name|Writable
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|declaredClass
decl_stmt|;
specifier|public
name|NullInstance
parameter_list|()
block|{
name|super
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|NullInstance
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|declaredClass
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|declaredClass
operator|=
name|declaredClass
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"boxing"
argument_list|)
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
name|declaredClass
operator|=
name|CODE_TO_CLASS
operator|.
name|get
argument_list|(
name|in
operator|.
name|readByte
argument_list|()
argument_list|)
expr_stmt|;
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
name|writeClassCode
argument_list|(
name|out
argument_list|,
name|this
operator|.
name|declaredClass
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Write out the code byte for passed Class.    * @param out    * @param c    * @throws IOException    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"boxing"
argument_list|)
specifier|static
name|void
name|writeClassCode
parameter_list|(
specifier|final
name|DataOutput
name|out
parameter_list|,
specifier|final
name|Class
argument_list|<
name|?
argument_list|>
name|c
parameter_list|)
throws|throws
name|IOException
block|{
name|Byte
name|code
init|=
name|CLASS_TO_CODE
operator|.
name|get
argument_list|(
name|c
argument_list|)
decl_stmt|;
if|if
condition|(
name|code
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Unsupported type "
operator|+
name|c
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"No code for unexpected "
operator|+
name|c
argument_list|)
throw|;
block|}
name|out
operator|.
name|writeByte
argument_list|(
name|code
argument_list|)
expr_stmt|;
block|}
comment|/** Write a {@link Writable}, {@link String}, primitive type, or an array of    * the preceding. */
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"boxing"
block|,
literal|"unchecked"
block|}
argument_list|)
specifier|public
specifier|static
name|void
name|writeObject
parameter_list|(
name|DataOutput
name|out
parameter_list|,
name|Object
name|instance
parameter_list|,
name|Class
name|declaredClass
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|instance
operator|==
literal|null
condition|)
block|{
comment|// null
name|instance
operator|=
operator|new
name|NullInstance
argument_list|(
name|declaredClass
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|declaredClass
operator|=
name|Writable
operator|.
name|class
expr_stmt|;
block|}
name|writeClassCode
argument_list|(
name|out
argument_list|,
name|declaredClass
argument_list|)
expr_stmt|;
if|if
condition|(
name|declaredClass
operator|.
name|isArray
argument_list|()
condition|)
block|{
comment|// array
name|int
name|length
init|=
name|Array
operator|.
name|getLength
argument_list|(
name|instance
argument_list|)
decl_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|length
argument_list|)
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
name|length
condition|;
name|i
operator|++
control|)
block|{
name|writeObject
argument_list|(
name|out
argument_list|,
name|Array
operator|.
name|get
argument_list|(
name|instance
argument_list|,
name|i
argument_list|)
argument_list|,
name|declaredClass
operator|.
name|getComponentType
argument_list|()
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|declaredClass
operator|==
name|String
operator|.
name|class
condition|)
block|{
comment|// String
name|Text
operator|.
name|writeString
argument_list|(
name|out
argument_list|,
operator|(
name|String
operator|)
name|instance
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|declaredClass
operator|.
name|isPrimitive
argument_list|()
condition|)
block|{
comment|// primitive type
if|if
condition|(
name|declaredClass
operator|==
name|Boolean
operator|.
name|TYPE
condition|)
block|{
comment|// boolean
name|out
operator|.
name|writeBoolean
argument_list|(
operator|(
operator|(
name|Boolean
operator|)
name|instance
operator|)
operator|.
name|booleanValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|declaredClass
operator|==
name|Character
operator|.
name|TYPE
condition|)
block|{
comment|// char
name|out
operator|.
name|writeChar
argument_list|(
operator|(
operator|(
name|Character
operator|)
name|instance
operator|)
operator|.
name|charValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|declaredClass
operator|==
name|Byte
operator|.
name|TYPE
condition|)
block|{
comment|// byte
name|out
operator|.
name|writeByte
argument_list|(
operator|(
operator|(
name|Byte
operator|)
name|instance
operator|)
operator|.
name|byteValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|declaredClass
operator|==
name|Short
operator|.
name|TYPE
condition|)
block|{
comment|// short
name|out
operator|.
name|writeShort
argument_list|(
operator|(
operator|(
name|Short
operator|)
name|instance
operator|)
operator|.
name|shortValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|declaredClass
operator|==
name|Integer
operator|.
name|TYPE
condition|)
block|{
comment|// int
name|out
operator|.
name|writeInt
argument_list|(
operator|(
operator|(
name|Integer
operator|)
name|instance
operator|)
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|declaredClass
operator|==
name|Long
operator|.
name|TYPE
condition|)
block|{
comment|// long
name|out
operator|.
name|writeLong
argument_list|(
operator|(
operator|(
name|Long
operator|)
name|instance
operator|)
operator|.
name|longValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|declaredClass
operator|==
name|Float
operator|.
name|TYPE
condition|)
block|{
comment|// float
name|out
operator|.
name|writeFloat
argument_list|(
operator|(
operator|(
name|Float
operator|)
name|instance
operator|)
operator|.
name|floatValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|declaredClass
operator|==
name|Double
operator|.
name|TYPE
condition|)
block|{
comment|// double
name|out
operator|.
name|writeDouble
argument_list|(
operator|(
operator|(
name|Double
operator|)
name|instance
operator|)
operator|.
name|doubleValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|declaredClass
operator|==
name|Void
operator|.
name|TYPE
condition|)
block|{
comment|// void
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Not a primitive: "
operator|+
name|declaredClass
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
name|declaredClass
operator|.
name|isEnum
argument_list|()
condition|)
block|{
comment|// enum
name|Text
operator|.
name|writeString
argument_list|(
name|out
argument_list|,
operator|(
operator|(
name|Enum
operator|)
name|instance
operator|)
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|Writable
operator|.
name|class
operator|.
name|isAssignableFrom
argument_list|(
name|declaredClass
argument_list|)
condition|)
block|{
comment|// Writable
name|Class
argument_list|<
name|?
argument_list|>
name|c
init|=
name|instance
operator|.
name|getClass
argument_list|()
decl_stmt|;
name|Byte
name|code
init|=
name|CLASS_TO_CODE
operator|.
name|get
argument_list|(
name|c
argument_list|)
decl_stmt|;
if|if
condition|(
name|code
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|writeByte
argument_list|(
name|NOT_ENCODED
argument_list|)
expr_stmt|;
name|Text
operator|.
name|writeString
argument_list|(
name|out
argument_list|,
name|c
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|writeClassCode
argument_list|(
name|out
argument_list|,
name|c
argument_list|)
expr_stmt|;
block|}
operator|(
operator|(
name|Writable
operator|)
name|instance
operator|)
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Can't write: "
operator|+
name|instance
operator|+
literal|" as "
operator|+
name|declaredClass
argument_list|)
throw|;
block|}
block|}
comment|/** Read a {@link Writable}, {@link String}, primitive type, or an array of    * the preceding. */
specifier|public
specifier|static
name|Object
name|readObject
parameter_list|(
name|DataInput
name|in
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|readObject
argument_list|(
name|in
argument_list|,
literal|null
argument_list|,
name|conf
argument_list|)
return|;
block|}
comment|/** Read a {@link Writable}, {@link String}, primitive type, or an array of    * the preceding. */
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"unchecked"
block|,
literal|"boxing"
block|}
argument_list|)
specifier|public
specifier|static
name|Object
name|readObject
parameter_list|(
name|DataInput
name|in
parameter_list|,
name|HbaseObjectWritable
name|objectWritable
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|declaredClass
init|=
name|CODE_TO_CLASS
operator|.
name|get
argument_list|(
name|in
operator|.
name|readByte
argument_list|()
argument_list|)
decl_stmt|;
name|Object
name|instance
decl_stmt|;
if|if
condition|(
name|declaredClass
operator|.
name|isPrimitive
argument_list|()
condition|)
block|{
comment|// primitive types
if|if
condition|(
name|declaredClass
operator|==
name|Boolean
operator|.
name|TYPE
condition|)
block|{
comment|// boolean
name|instance
operator|=
name|Boolean
operator|.
name|valueOf
argument_list|(
name|in
operator|.
name|readBoolean
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|declaredClass
operator|==
name|Character
operator|.
name|TYPE
condition|)
block|{
comment|// char
name|instance
operator|=
name|Character
operator|.
name|valueOf
argument_list|(
name|in
operator|.
name|readChar
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|declaredClass
operator|==
name|Byte
operator|.
name|TYPE
condition|)
block|{
comment|// byte
name|instance
operator|=
name|Byte
operator|.
name|valueOf
argument_list|(
name|in
operator|.
name|readByte
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|declaredClass
operator|==
name|Short
operator|.
name|TYPE
condition|)
block|{
comment|// short
name|instance
operator|=
name|Short
operator|.
name|valueOf
argument_list|(
name|in
operator|.
name|readShort
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|declaredClass
operator|==
name|Integer
operator|.
name|TYPE
condition|)
block|{
comment|// int
name|instance
operator|=
name|Integer
operator|.
name|valueOf
argument_list|(
name|in
operator|.
name|readInt
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|declaredClass
operator|==
name|Long
operator|.
name|TYPE
condition|)
block|{
comment|// long
name|instance
operator|=
name|Long
operator|.
name|valueOf
argument_list|(
name|in
operator|.
name|readLong
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|declaredClass
operator|==
name|Float
operator|.
name|TYPE
condition|)
block|{
comment|// float
name|instance
operator|=
name|Float
operator|.
name|valueOf
argument_list|(
name|in
operator|.
name|readFloat
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|declaredClass
operator|==
name|Double
operator|.
name|TYPE
condition|)
block|{
comment|// double
name|instance
operator|=
name|Double
operator|.
name|valueOf
argument_list|(
name|in
operator|.
name|readDouble
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|declaredClass
operator|==
name|Void
operator|.
name|TYPE
condition|)
block|{
comment|// void
name|instance
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Not a primitive: "
operator|+
name|declaredClass
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
name|declaredClass
operator|.
name|isArray
argument_list|()
condition|)
block|{
comment|// array
name|int
name|length
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|instance
operator|=
name|Array
operator|.
name|newInstance
argument_list|(
name|declaredClass
operator|.
name|getComponentType
argument_list|()
argument_list|,
name|length
argument_list|)
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
name|length
condition|;
name|i
operator|++
control|)
block|{
name|Array
operator|.
name|set
argument_list|(
name|instance
argument_list|,
name|i
argument_list|,
name|readObject
argument_list|(
name|in
argument_list|,
name|conf
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|declaredClass
operator|==
name|String
operator|.
name|class
condition|)
block|{
comment|// String
name|instance
operator|=
name|Text
operator|.
name|readString
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|declaredClass
operator|.
name|isEnum
argument_list|()
condition|)
block|{
comment|// enum
name|instance
operator|=
name|Enum
operator|.
name|valueOf
argument_list|(
operator|(
name|Class
argument_list|<
name|?
extends|extends
name|Enum
argument_list|>
operator|)
name|declaredClass
argument_list|,
name|Text
operator|.
name|readString
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Writable
name|Class
argument_list|<
name|?
argument_list|>
name|instanceClass
init|=
literal|null
decl_stmt|;
name|Byte
name|b
init|=
name|in
operator|.
name|readByte
argument_list|()
decl_stmt|;
if|if
condition|(
name|b
operator|.
name|byteValue
argument_list|()
operator|==
name|NOT_ENCODED
condition|)
block|{
name|String
name|className
init|=
name|Text
operator|.
name|readString
argument_list|(
name|in
argument_list|)
decl_stmt|;
try|try
block|{
name|instanceClass
operator|=
name|conf
operator|.
name|getClassByName
argument_list|(
name|className
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Can't find class "
operator|+
name|className
argument_list|)
throw|;
block|}
block|}
else|else
block|{
name|instanceClass
operator|=
name|CODE_TO_CLASS
operator|.
name|get
argument_list|(
name|b
argument_list|)
expr_stmt|;
block|}
name|Writable
name|writable
init|=
name|WritableFactories
operator|.
name|newInstance
argument_list|(
name|instanceClass
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|writable
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|instance
operator|=
name|writable
expr_stmt|;
if|if
condition|(
name|instanceClass
operator|==
name|NullInstance
operator|.
name|class
condition|)
block|{
comment|// null
name|declaredClass
operator|=
operator|(
operator|(
name|NullInstance
operator|)
name|instance
operator|)
operator|.
name|declaredClass
expr_stmt|;
name|instance
operator|=
literal|null
expr_stmt|;
block|}
block|}
if|if
condition|(
name|objectWritable
operator|!=
literal|null
condition|)
block|{
comment|// store values
name|objectWritable
operator|.
name|declaredClass
operator|=
name|declaredClass
expr_stmt|;
name|objectWritable
operator|.
name|instance
operator|=
name|instance
expr_stmt|;
block|}
return|return
name|instance
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"boxing"
argument_list|)
specifier|private
specifier|static
name|void
name|addToMap
parameter_list|(
specifier|final
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
parameter_list|,
specifier|final
name|byte
name|code
parameter_list|)
block|{
name|CLASS_TO_CODE
operator|.
name|put
argument_list|(
name|clazz
argument_list|,
name|code
argument_list|)
expr_stmt|;
name|CODE_TO_CLASS
operator|.
name|put
argument_list|(
name|code
argument_list|,
name|clazz
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
block|}
specifier|public
name|Configuration
name|getConf
parameter_list|()
block|{
return|return
name|this
operator|.
name|conf
return|;
block|}
block|}
end_class

end_unit

