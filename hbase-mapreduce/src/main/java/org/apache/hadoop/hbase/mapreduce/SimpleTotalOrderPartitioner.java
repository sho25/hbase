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
name|mapreduce
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Base64
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|util
operator|.
name|Bytes
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
name|mapreduce
operator|.
name|Partitioner
import|;
end_import

begin_comment
comment|/**  * A partitioner that takes start and end keys and uses bigdecimal to figure  * which reduce a key belongs to.  Pass the start and end  * keys in the Configuration using<code>hbase.simpletotalorder.start</code>  * and<code>hbase.simpletotalorder.end</code>.  The end key needs to be  * exclusive; i.e. one larger than the biggest key in your key space.  * You may be surprised at how this class partitions the space; it may not  * align with preconceptions; e.g. a start key of zero and an end key of 100  * divided in ten will not make regions whose range is 0-10, 10-20, and so on.  * Make your own partitioner if you need the region spacing to come out a  * particular way.  * @param<VALUE>  * @see #START  * @see #END  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|SimpleTotalOrderPartitioner
parameter_list|<
name|VALUE
parameter_list|>
extends|extends
name|Partitioner
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|VALUE
argument_list|>
implements|implements
name|Configurable
block|{
specifier|private
specifier|final
specifier|static
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|SimpleTotalOrderPartitioner
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * @deprecated since 0.90.0    * @see<a href="https://issues.apache.org/jira/browse/HBASE-1923">HBASE-1923</a>    */
annotation|@
name|Deprecated
specifier|public
specifier|static
specifier|final
name|String
name|START
init|=
literal|"hbase.simpletotalorder.start"
decl_stmt|;
comment|/**    * @deprecated since 0.90.0    * @see<a href="https://issues.apache.org/jira/browse/HBASE-1923">HBASE-1923</a>    */
annotation|@
name|Deprecated
specifier|public
specifier|static
specifier|final
name|String
name|END
init|=
literal|"hbase.simpletotalorder.end"
decl_stmt|;
specifier|static
specifier|final
name|String
name|START_BASE64
init|=
literal|"hbase.simpletotalorder.start.base64"
decl_stmt|;
specifier|static
specifier|final
name|String
name|END_BASE64
init|=
literal|"hbase.simpletotalorder.end.base64"
decl_stmt|;
specifier|private
name|Configuration
name|c
decl_stmt|;
specifier|private
name|byte
index|[]
name|startkey
decl_stmt|;
specifier|private
name|byte
index|[]
name|endkey
decl_stmt|;
specifier|private
name|byte
index|[]
index|[]
name|splits
decl_stmt|;
specifier|private
name|int
name|lastReduces
init|=
operator|-
literal|1
decl_stmt|;
specifier|public
specifier|static
name|void
name|setStartKey
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|byte
index|[]
name|startKey
parameter_list|)
block|{
name|conf
operator|.
name|set
argument_list|(
name|START_BASE64
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|Base64
operator|.
name|getEncoder
argument_list|()
operator|.
name|encode
argument_list|(
name|startKey
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|setEndKey
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|byte
index|[]
name|endKey
parameter_list|)
block|{
name|conf
operator|.
name|set
argument_list|(
name|END_BASE64
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|Base64
operator|.
name|getEncoder
argument_list|()
operator|.
name|encode
argument_list|(
name|endKey
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
specifier|static
name|byte
index|[]
name|getStartKey
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
return|return
name|getKeyFromConf
argument_list|(
name|conf
argument_list|,
name|START_BASE64
argument_list|,
name|START
argument_list|)
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
specifier|static
name|byte
index|[]
name|getEndKey
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
return|return
name|getKeyFromConf
argument_list|(
name|conf
argument_list|,
name|END_BASE64
argument_list|,
name|END
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|byte
index|[]
name|getKeyFromConf
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|base64Key
parameter_list|,
name|String
name|deprecatedKey
parameter_list|)
block|{
name|String
name|encoded
init|=
name|conf
operator|.
name|get
argument_list|(
name|base64Key
argument_list|)
decl_stmt|;
if|if
condition|(
name|encoded
operator|!=
literal|null
condition|)
block|{
return|return
name|Base64
operator|.
name|getDecoder
argument_list|()
operator|.
name|decode
argument_list|(
name|encoded
argument_list|)
return|;
block|}
name|String
name|oldStyleVal
init|=
name|conf
operator|.
name|get
argument_list|(
name|deprecatedKey
argument_list|)
decl_stmt|;
if|if
condition|(
name|oldStyleVal
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|LOG
operator|.
name|warn
argument_list|(
literal|"Using deprecated configuration "
operator|+
name|deprecatedKey
operator|+
literal|" - please use static accessor methods instead."
argument_list|)
expr_stmt|;
return|return
name|Bytes
operator|.
name|toBytesBinary
argument_list|(
name|oldStyleVal
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getPartition
parameter_list|(
specifier|final
name|ImmutableBytesWritable
name|key
parameter_list|,
specifier|final
name|VALUE
name|value
parameter_list|,
specifier|final
name|int
name|reduces
parameter_list|)
block|{
if|if
condition|(
name|reduces
operator|==
literal|1
condition|)
return|return
literal|0
return|;
if|if
condition|(
name|this
operator|.
name|lastReduces
operator|!=
name|reduces
condition|)
block|{
name|this
operator|.
name|splits
operator|=
name|Bytes
operator|.
name|split
argument_list|(
name|this
operator|.
name|startkey
argument_list|,
name|this
operator|.
name|endkey
argument_list|,
name|reduces
operator|-
literal|1
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
name|splits
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|splits
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|lastReduces
operator|=
name|reduces
expr_stmt|;
block|}
name|int
name|pos
init|=
name|Bytes
operator|.
name|binarySearch
argument_list|(
name|this
operator|.
name|splits
argument_list|,
name|key
operator|.
name|get
argument_list|()
argument_list|,
name|key
operator|.
name|getOffset
argument_list|()
argument_list|,
name|key
operator|.
name|getLength
argument_list|()
argument_list|)
decl_stmt|;
comment|// Below code is from hfile index search.
if|if
condition|(
name|pos
operator|<
literal|0
condition|)
block|{
name|pos
operator|++
expr_stmt|;
name|pos
operator|*=
operator|-
literal|1
expr_stmt|;
if|if
condition|(
name|pos
operator|==
literal|0
condition|)
block|{
comment|// falls before the beginning of the file.
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Key outside start/stop range: "
operator|+
name|key
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
name|pos
operator|--
expr_stmt|;
block|}
return|return
name|pos
return|;
block|}
annotation|@
name|Override
specifier|public
name|Configuration
name|getConf
parameter_list|()
block|{
return|return
name|this
operator|.
name|c
return|;
block|}
annotation|@
name|Override
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
name|c
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|startkey
operator|=
name|getStartKey
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|endkey
operator|=
name|getEndKey
argument_list|(
name|conf
argument_list|)
expr_stmt|;
if|if
condition|(
name|startkey
operator|==
literal|null
operator|||
name|endkey
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
operator|+
literal|" not configured"
argument_list|)
throw|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"startkey="
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|startkey
argument_list|)
operator|+
literal|", endkey="
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|endkey
argument_list|)
argument_list|)
expr_stmt|;
comment|// Reset last reduces count on change of Start / End key
name|this
operator|.
name|lastReduces
operator|=
operator|-
literal|1
expr_stmt|;
block|}
block|}
end_class

end_unit

