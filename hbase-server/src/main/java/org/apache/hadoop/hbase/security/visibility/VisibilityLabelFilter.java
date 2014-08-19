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
name|security
operator|.
name|visibility
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
name|BitSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
name|hadoop
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
name|Tag
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
name|io
operator|.
name|util
operator|.
name|StreamUtils
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
name|ByteRange
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
name|hbase
operator|.
name|util
operator|.
name|Pair
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
name|SimpleMutableByteRange
import|;
end_import

begin_comment
comment|/**  * This Filter checks the visibility expression with each KV against visibility labels associated  * with the scan. Based on the check the KV is included in the scan result or gets filtered out.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|VisibilityLabelFilter
extends|extends
name|FilterBase
block|{
specifier|private
specifier|final
name|BitSet
name|authLabels
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|ByteRange
argument_list|,
name|Integer
argument_list|>
name|cfVsMaxVersions
decl_stmt|;
specifier|private
specifier|final
name|ByteRange
name|curFamily
decl_stmt|;
specifier|private
specifier|final
name|ByteRange
name|curQualifier
decl_stmt|;
specifier|private
name|int
name|curFamilyMaxVersions
decl_stmt|;
specifier|private
name|int
name|curQualMetVersions
decl_stmt|;
specifier|public
name|VisibilityLabelFilter
parameter_list|(
name|BitSet
name|authLabels
parameter_list|,
name|Map
argument_list|<
name|ByteRange
argument_list|,
name|Integer
argument_list|>
name|cfVsMaxVersions
parameter_list|)
block|{
name|this
operator|.
name|authLabels
operator|=
name|authLabels
expr_stmt|;
name|this
operator|.
name|cfVsMaxVersions
operator|=
name|cfVsMaxVersions
expr_stmt|;
name|this
operator|.
name|curFamily
operator|=
operator|new
name|SimpleMutableByteRange
argument_list|()
expr_stmt|;
name|this
operator|.
name|curQualifier
operator|=
operator|new
name|SimpleMutableByteRange
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ReturnCode
name|filterKeyValue
parameter_list|(
name|Cell
name|cell
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|curFamily
operator|.
name|getBytes
argument_list|()
operator|==
literal|null
operator|||
operator|(
name|Bytes
operator|.
name|compareTo
argument_list|(
name|curFamily
operator|.
name|getBytes
argument_list|()
argument_list|,
name|curFamily
operator|.
name|getOffset
argument_list|()
argument_list|,
name|curFamily
operator|.
name|getLength
argument_list|()
argument_list|,
name|cell
operator|.
name|getFamilyArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getFamilyOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getFamilyLength
argument_list|()
argument_list|)
operator|!=
literal|0
operator|)
condition|)
block|{
name|curFamily
operator|.
name|set
argument_list|(
name|cell
operator|.
name|getFamilyArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getFamilyOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getFamilyLength
argument_list|()
argument_list|)
expr_stmt|;
comment|// For this family, all the columns can have max of curFamilyMaxVersions versions. No need to
comment|// consider the older versions for visibility label check.
comment|// Ideally this should have been done at a lower layer by HBase (?)
name|curFamilyMaxVersions
operator|=
name|cfVsMaxVersions
operator|.
name|get
argument_list|(
name|curFamily
argument_list|)
expr_stmt|;
comment|// Family is changed. Just unset curQualifier.
name|curQualifier
operator|.
name|unset
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|curQualifier
operator|.
name|getBytes
argument_list|()
operator|==
literal|null
operator|||
operator|(
name|Bytes
operator|.
name|compareTo
argument_list|(
name|curQualifier
operator|.
name|getBytes
argument_list|()
argument_list|,
name|curQualifier
operator|.
name|getOffset
argument_list|()
argument_list|,
name|curQualifier
operator|.
name|getLength
argument_list|()
argument_list|,
name|cell
operator|.
name|getQualifierArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getQualifierOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getQualifierLength
argument_list|()
argument_list|)
operator|!=
literal|0
operator|)
condition|)
block|{
name|curQualifier
operator|.
name|set
argument_list|(
name|cell
operator|.
name|getQualifierArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getQualifierOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getQualifierLength
argument_list|()
argument_list|)
expr_stmt|;
name|curQualMetVersions
operator|=
literal|0
expr_stmt|;
block|}
name|curQualMetVersions
operator|++
expr_stmt|;
if|if
condition|(
name|curQualMetVersions
operator|>
name|curFamilyMaxVersions
condition|)
block|{
return|return
name|ReturnCode
operator|.
name|SKIP
return|;
block|}
name|boolean
name|visibilityTagPresent
init|=
literal|false
decl_stmt|;
comment|// Save an object allocation where we can
if|if
condition|(
name|cell
operator|.
name|getTagsLength
argument_list|()
operator|>
literal|0
condition|)
block|{
name|Iterator
argument_list|<
name|Tag
argument_list|>
name|tagsItr
init|=
name|CellUtil
operator|.
name|tagsIterator
argument_list|(
name|cell
operator|.
name|getTagsArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getTagsOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getTagsLength
argument_list|()
argument_list|)
decl_stmt|;
while|while
condition|(
name|tagsItr
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|boolean
name|includeKV
init|=
literal|true
decl_stmt|;
name|Tag
name|tag
init|=
name|tagsItr
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|tag
operator|.
name|getType
argument_list|()
operator|==
name|VisibilityUtils
operator|.
name|VISIBILITY_TAG_TYPE
condition|)
block|{
name|visibilityTagPresent
operator|=
literal|true
expr_stmt|;
name|int
name|offset
init|=
name|tag
operator|.
name|getTagOffset
argument_list|()
decl_stmt|;
name|int
name|endOffset
init|=
name|offset
operator|+
name|tag
operator|.
name|getTagLength
argument_list|()
decl_stmt|;
while|while
condition|(
name|offset
operator|<
name|endOffset
condition|)
block|{
name|Pair
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
name|result
init|=
name|StreamUtils
operator|.
name|readRawVarint32
argument_list|(
name|tag
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|offset
argument_list|)
decl_stmt|;
name|int
name|currLabelOrdinal
init|=
name|result
operator|.
name|getFirst
argument_list|()
decl_stmt|;
if|if
condition|(
name|currLabelOrdinal
operator|<
literal|0
condition|)
block|{
comment|// check for the absence of this label in the Scan Auth labels
comment|// ie. to check BitSet corresponding bit is 0
name|int
name|temp
init|=
operator|-
name|currLabelOrdinal
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|authLabels
operator|.
name|get
argument_list|(
name|temp
argument_list|)
condition|)
block|{
name|includeKV
operator|=
literal|false
expr_stmt|;
break|break;
block|}
block|}
else|else
block|{
if|if
condition|(
operator|!
name|this
operator|.
name|authLabels
operator|.
name|get
argument_list|(
name|currLabelOrdinal
argument_list|)
condition|)
block|{
name|includeKV
operator|=
literal|false
expr_stmt|;
break|break;
block|}
block|}
name|offset
operator|+=
name|result
operator|.
name|getSecond
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|includeKV
condition|)
block|{
comment|// We got one visibility expression getting evaluated to true. Good to include this KV in
comment|// the result then.
return|return
name|ReturnCode
operator|.
name|INCLUDE
return|;
block|}
block|}
block|}
block|}
return|return
name|visibilityTagPresent
condition|?
name|ReturnCode
operator|.
name|SKIP
else|:
name|ReturnCode
operator|.
name|INCLUDE
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|reset
parameter_list|()
throws|throws
name|IOException
block|{
name|this
operator|.
name|curFamily
operator|.
name|unset
argument_list|()
expr_stmt|;
name|this
operator|.
name|curQualifier
operator|.
name|unset
argument_list|()
expr_stmt|;
name|this
operator|.
name|curFamilyMaxVersions
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|curQualMetVersions
operator|=
literal|0
expr_stmt|;
block|}
block|}
end_class

end_unit

