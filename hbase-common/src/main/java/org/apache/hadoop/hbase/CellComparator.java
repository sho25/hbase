begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Serializable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Comparator
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
name|classification
operator|.
name|InterfaceStability
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
name|KeyValue
operator|.
name|Type
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|primitives
operator|.
name|Longs
import|;
end_import

begin_comment
comment|/**  * Compare two HBase cells.  Do not use this method comparing<code>-ROOT-</code> or  *<code>hbase:meta</code> cells.  Cells from these tables need a specialized comparator, one that  * takes account of the special formatting of the row where we have commas to delimit table from  * regionname, from row.  See KeyValue for how it has a special comparator to do hbase:meta cells  * and yet another for -ROOT-.  */
end_comment

begin_class
annotation|@
name|edu
operator|.
name|umd
operator|.
name|cs
operator|.
name|findbugs
operator|.
name|annotations
operator|.
name|SuppressWarnings
argument_list|(
name|value
operator|=
literal|"UNKNOWN"
argument_list|,
name|justification
operator|=
literal|"Findbugs doesn't like the way we are negating the result of a compare in below"
argument_list|)
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|CellComparator
implements|implements
name|Comparator
argument_list|<
name|Cell
argument_list|>
implements|,
name|Serializable
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
operator|-
literal|8760041766259623329L
decl_stmt|;
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|Cell
name|a
parameter_list|,
name|Cell
name|b
parameter_list|)
block|{
return|return
name|compareStatic
argument_list|(
name|a
argument_list|,
name|b
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|int
name|compareStatic
parameter_list|(
name|Cell
name|a
parameter_list|,
name|Cell
name|b
parameter_list|)
block|{
comment|//row
name|int
name|c
init|=
name|Bytes
operator|.
name|compareTo
argument_list|(
name|a
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|a
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|a
operator|.
name|getRowLength
argument_list|()
argument_list|,
name|b
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|b
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|b
operator|.
name|getRowLength
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|c
operator|!=
literal|0
condition|)
return|return
name|c
return|;
comment|// If the column is not specified, the "minimum" key type appears the
comment|// latest in the sorted order, regardless of the timestamp. This is used
comment|// for specifying the last key/value in a given row, because there is no
comment|// "lexicographically last column" (it would be infinitely long). The
comment|// "maximum" key type does not need this behavior.
if|if
condition|(
name|a
operator|.
name|getFamilyLength
argument_list|()
operator|==
literal|0
operator|&&
name|a
operator|.
name|getTypeByte
argument_list|()
operator|==
name|Type
operator|.
name|Minimum
operator|.
name|getCode
argument_list|()
condition|)
block|{
comment|// a is "bigger", i.e. it appears later in the sorted order
return|return
literal|1
return|;
block|}
if|if
condition|(
name|b
operator|.
name|getFamilyLength
argument_list|()
operator|==
literal|0
operator|&&
name|b
operator|.
name|getTypeByte
argument_list|()
operator|==
name|Type
operator|.
name|Minimum
operator|.
name|getCode
argument_list|()
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
comment|//family
name|c
operator|=
name|Bytes
operator|.
name|compareTo
argument_list|(
name|a
operator|.
name|getFamilyArray
argument_list|()
argument_list|,
name|a
operator|.
name|getFamilyOffset
argument_list|()
argument_list|,
name|a
operator|.
name|getFamilyLength
argument_list|()
argument_list|,
name|b
operator|.
name|getFamilyArray
argument_list|()
argument_list|,
name|b
operator|.
name|getFamilyOffset
argument_list|()
argument_list|,
name|b
operator|.
name|getFamilyLength
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|c
operator|!=
literal|0
condition|)
return|return
name|c
return|;
comment|//qualifier
name|c
operator|=
name|Bytes
operator|.
name|compareTo
argument_list|(
name|a
operator|.
name|getQualifierArray
argument_list|()
argument_list|,
name|a
operator|.
name|getQualifierOffset
argument_list|()
argument_list|,
name|a
operator|.
name|getQualifierLength
argument_list|()
argument_list|,
name|b
operator|.
name|getQualifierArray
argument_list|()
argument_list|,
name|b
operator|.
name|getQualifierOffset
argument_list|()
argument_list|,
name|b
operator|.
name|getQualifierLength
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|c
operator|!=
literal|0
condition|)
return|return
name|c
return|;
comment|//timestamp: later sorts first
name|c
operator|=
name|Longs
operator|.
name|compare
argument_list|(
name|b
operator|.
name|getTimestamp
argument_list|()
argument_list|,
name|a
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|c
operator|!=
literal|0
condition|)
return|return
name|c
return|;
comment|//type
name|c
operator|=
operator|(
literal|0xff
operator|&
name|a
operator|.
name|getTypeByte
argument_list|()
operator|)
operator|-
operator|(
literal|0xff
operator|&
name|b
operator|.
name|getTypeByte
argument_list|()
operator|)
expr_stmt|;
if|if
condition|(
name|c
operator|!=
literal|0
condition|)
return|return
name|c
return|;
comment|//mvccVersion: later sorts first
return|return
name|Longs
operator|.
name|compare
argument_list|(
name|b
operator|.
name|getMvccVersion
argument_list|()
argument_list|,
name|a
operator|.
name|getMvccVersion
argument_list|()
argument_list|)
return|;
block|}
comment|/**************** equals ****************************/
specifier|public
specifier|static
name|boolean
name|equals
parameter_list|(
name|Cell
name|a
parameter_list|,
name|Cell
name|b
parameter_list|)
block|{
return|return
name|equalsRow
argument_list|(
name|a
argument_list|,
name|b
argument_list|)
operator|&&
name|equalsFamily
argument_list|(
name|a
argument_list|,
name|b
argument_list|)
operator|&&
name|equalsQualifier
argument_list|(
name|a
argument_list|,
name|b
argument_list|)
operator|&&
name|equalsTimestamp
argument_list|(
name|a
argument_list|,
name|b
argument_list|)
operator|&&
name|equalsType
argument_list|(
name|a
argument_list|,
name|b
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|boolean
name|equalsRow
parameter_list|(
name|Cell
name|a
parameter_list|,
name|Cell
name|b
parameter_list|)
block|{
return|return
name|Bytes
operator|.
name|equals
argument_list|(
name|a
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|a
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|a
operator|.
name|getRowLength
argument_list|()
argument_list|,
name|b
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|b
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|b
operator|.
name|getRowLength
argument_list|()
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|boolean
name|equalsFamily
parameter_list|(
name|Cell
name|a
parameter_list|,
name|Cell
name|b
parameter_list|)
block|{
return|return
name|Bytes
operator|.
name|equals
argument_list|(
name|a
operator|.
name|getFamilyArray
argument_list|()
argument_list|,
name|a
operator|.
name|getFamilyOffset
argument_list|()
argument_list|,
name|a
operator|.
name|getFamilyLength
argument_list|()
argument_list|,
name|b
operator|.
name|getFamilyArray
argument_list|()
argument_list|,
name|b
operator|.
name|getFamilyOffset
argument_list|()
argument_list|,
name|b
operator|.
name|getFamilyLength
argument_list|()
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|boolean
name|equalsQualifier
parameter_list|(
name|Cell
name|a
parameter_list|,
name|Cell
name|b
parameter_list|)
block|{
return|return
name|Bytes
operator|.
name|equals
argument_list|(
name|a
operator|.
name|getQualifierArray
argument_list|()
argument_list|,
name|a
operator|.
name|getQualifierOffset
argument_list|()
argument_list|,
name|a
operator|.
name|getQualifierLength
argument_list|()
argument_list|,
name|b
operator|.
name|getQualifierArray
argument_list|()
argument_list|,
name|b
operator|.
name|getQualifierOffset
argument_list|()
argument_list|,
name|b
operator|.
name|getQualifierLength
argument_list|()
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|boolean
name|equalsTimestamp
parameter_list|(
name|Cell
name|a
parameter_list|,
name|Cell
name|b
parameter_list|)
block|{
return|return
name|a
operator|.
name|getTimestamp
argument_list|()
operator|==
name|b
operator|.
name|getTimestamp
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|boolean
name|equalsType
parameter_list|(
name|Cell
name|a
parameter_list|,
name|Cell
name|b
parameter_list|)
block|{
return|return
name|a
operator|.
name|getTypeByte
argument_list|()
operator|==
name|b
operator|.
name|getTypeByte
argument_list|()
return|;
block|}
comment|/********************* hashCode ************************/
comment|/**    * Returns a hash code that is always the same for two Cells having a matching equals(..) result.    * Currently does not guard against nulls, but it could if necessary.    */
specifier|public
specifier|static
name|int
name|hashCode
parameter_list|(
name|Cell
name|cell
parameter_list|)
block|{
if|if
condition|(
name|cell
operator|==
literal|null
condition|)
block|{
comment|// return 0 for empty Cell
return|return
literal|0
return|;
block|}
comment|//pre-calculate the 3 hashes made of byte ranges
name|int
name|rowHash
init|=
name|Bytes
operator|.
name|hashCode
argument_list|(
name|cell
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getRowLength
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|familyHash
init|=
name|Bytes
operator|.
name|hashCode
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
decl_stmt|;
name|int
name|qualifierHash
init|=
name|Bytes
operator|.
name|hashCode
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
decl_stmt|;
comment|//combine the 6 sub-hashes
name|int
name|hash
init|=
literal|31
operator|*
name|rowHash
operator|+
name|familyHash
decl_stmt|;
name|hash
operator|=
literal|31
operator|*
name|hash
operator|+
name|qualifierHash
expr_stmt|;
name|hash
operator|=
literal|31
operator|*
name|hash
operator|+
operator|(
name|int
operator|)
name|cell
operator|.
name|getTimestamp
argument_list|()
expr_stmt|;
name|hash
operator|=
literal|31
operator|*
name|hash
operator|+
name|cell
operator|.
name|getTypeByte
argument_list|()
expr_stmt|;
name|hash
operator|=
literal|31
operator|*
name|hash
operator|+
operator|(
name|int
operator|)
name|cell
operator|.
name|getMvccVersion
argument_list|()
expr_stmt|;
return|return
name|hash
return|;
block|}
comment|/******************** lengths *************************/
specifier|public
specifier|static
name|boolean
name|areKeyLengthsEqual
parameter_list|(
name|Cell
name|a
parameter_list|,
name|Cell
name|b
parameter_list|)
block|{
return|return
name|a
operator|.
name|getRowLength
argument_list|()
operator|==
name|b
operator|.
name|getRowLength
argument_list|()
operator|&&
name|a
operator|.
name|getFamilyLength
argument_list|()
operator|==
name|b
operator|.
name|getFamilyLength
argument_list|()
operator|&&
name|a
operator|.
name|getQualifierLength
argument_list|()
operator|==
name|b
operator|.
name|getQualifierLength
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|boolean
name|areRowLengthsEqual
parameter_list|(
name|Cell
name|a
parameter_list|,
name|Cell
name|b
parameter_list|)
block|{
return|return
name|a
operator|.
name|getRowLength
argument_list|()
operator|==
name|b
operator|.
name|getRowLength
argument_list|()
return|;
block|}
comment|/***************** special cases ****************************/
comment|/**    * special case for KeyValue.equals    */
specifier|private
specifier|static
name|int
name|compareStaticIgnoreMvccVersion
parameter_list|(
name|Cell
name|a
parameter_list|,
name|Cell
name|b
parameter_list|)
block|{
comment|//row
name|int
name|c
init|=
name|Bytes
operator|.
name|compareTo
argument_list|(
name|a
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|a
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|a
operator|.
name|getRowLength
argument_list|()
argument_list|,
name|b
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|b
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|b
operator|.
name|getRowLength
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|c
operator|!=
literal|0
condition|)
return|return
name|c
return|;
comment|//family
name|c
operator|=
name|Bytes
operator|.
name|compareTo
argument_list|(
name|a
operator|.
name|getFamilyArray
argument_list|()
argument_list|,
name|a
operator|.
name|getFamilyOffset
argument_list|()
argument_list|,
name|a
operator|.
name|getFamilyLength
argument_list|()
argument_list|,
name|b
operator|.
name|getFamilyArray
argument_list|()
argument_list|,
name|b
operator|.
name|getFamilyOffset
argument_list|()
argument_list|,
name|b
operator|.
name|getFamilyLength
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|c
operator|!=
literal|0
condition|)
return|return
name|c
return|;
comment|//qualifier
name|c
operator|=
name|Bytes
operator|.
name|compareTo
argument_list|(
name|a
operator|.
name|getQualifierArray
argument_list|()
argument_list|,
name|a
operator|.
name|getQualifierOffset
argument_list|()
argument_list|,
name|a
operator|.
name|getQualifierLength
argument_list|()
argument_list|,
name|b
operator|.
name|getQualifierArray
argument_list|()
argument_list|,
name|b
operator|.
name|getQualifierOffset
argument_list|()
argument_list|,
name|b
operator|.
name|getQualifierLength
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|c
operator|!=
literal|0
condition|)
return|return
name|c
return|;
comment|//timestamp: later sorts first
name|c
operator|=
name|Longs
operator|.
name|compare
argument_list|(
name|b
operator|.
name|getTimestamp
argument_list|()
argument_list|,
name|a
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|c
operator|!=
literal|0
condition|)
return|return
name|c
return|;
comment|//type
name|c
operator|=
operator|(
literal|0xff
operator|&
name|a
operator|.
name|getTypeByte
argument_list|()
operator|)
operator|-
operator|(
literal|0xff
operator|&
name|b
operator|.
name|getTypeByte
argument_list|()
operator|)
expr_stmt|;
return|return
name|c
return|;
block|}
comment|/**    * special case for KeyValue.equals    */
specifier|public
specifier|static
name|boolean
name|equalsIgnoreMvccVersion
parameter_list|(
name|Cell
name|a
parameter_list|,
name|Cell
name|b
parameter_list|)
block|{
return|return
literal|0
operator|==
name|compareStaticIgnoreMvccVersion
argument_list|(
name|a
argument_list|,
name|b
argument_list|)
return|;
block|}
block|}
end_class

end_unit

