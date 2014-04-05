begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|access
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
name|TableName
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
name|exceptions
operator|.
name|DeserializationException
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
name|Filter
operator|.
name|ReturnCode
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
name|security
operator|.
name|User
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
name|SimpleByteRange
import|;
end_import

begin_comment
comment|/**  *<strong>NOTE: for internal use only by AccessController implementation</strong>  *  *<p>  * TODO: There is room for further performance optimization here.  * Calling TableAuthManager.authorize() per KeyValue imposes a fair amount of  * overhead.  A more optimized solution might look at the qualifiers where  * permissions are actually granted and explicitly limit the scan to those.  *</p>  *<p>  * We should aim to use this _only_ when access to the requested column families  * is not granted at the column family levels.  If table or column family  * access succeeds, then there is no need to impose the overhead of this filter.  *</p>  */
end_comment

begin_class
class|class
name|AccessControlFilter
extends|extends
name|FilterBase
block|{
specifier|private
name|TableAuthManager
name|authManager
decl_stmt|;
specifier|private
name|TableName
name|table
decl_stmt|;
specifier|private
name|User
name|user
decl_stmt|;
specifier|private
name|boolean
name|isSystemTable
decl_stmt|;
specifier|private
name|boolean
name|cellFirstStrategy
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|ByteRange
argument_list|,
name|Integer
argument_list|>
name|cfVsMaxVersions
decl_stmt|;
specifier|private
name|int
name|familyMaxVersions
decl_stmt|;
specifier|private
name|int
name|currentVersions
decl_stmt|;
specifier|private
name|ByteRange
name|prevFam
decl_stmt|;
specifier|private
name|ByteRange
name|prevQual
decl_stmt|;
comment|/**    * For Writable    */
name|AccessControlFilter
parameter_list|()
block|{   }
name|AccessControlFilter
parameter_list|(
name|TableAuthManager
name|mgr
parameter_list|,
name|User
name|ugi
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|boolean
name|cellFirstStrategy
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
name|authManager
operator|=
name|mgr
expr_stmt|;
name|table
operator|=
name|tableName
expr_stmt|;
name|user
operator|=
name|ugi
expr_stmt|;
name|isSystemTable
operator|=
name|tableName
operator|.
name|isSystemTable
argument_list|()
expr_stmt|;
name|this
operator|.
name|cellFirstStrategy
operator|=
name|cellFirstStrategy
expr_stmt|;
name|this
operator|.
name|cfVsMaxVersions
operator|=
name|cfVsMaxVersions
expr_stmt|;
name|this
operator|.
name|prevFam
operator|=
operator|new
name|SimpleByteRange
argument_list|()
expr_stmt|;
name|this
operator|.
name|prevQual
operator|=
operator|new
name|SimpleByteRange
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
block|{
if|if
condition|(
name|isSystemTable
condition|)
block|{
return|return
name|ReturnCode
operator|.
name|INCLUDE
return|;
block|}
if|if
condition|(
name|prevFam
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
name|prevFam
operator|.
name|getBytes
argument_list|()
argument_list|,
name|prevFam
operator|.
name|getOffset
argument_list|()
argument_list|,
name|prevFam
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
name|prevFam
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
comment|// Similar to VisibilityLabelFilter
name|familyMaxVersions
operator|=
name|cfVsMaxVersions
operator|.
name|get
argument_list|(
name|prevFam
argument_list|)
expr_stmt|;
comment|// Family is changed. Just unset curQualifier.
name|prevQual
operator|.
name|unset
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|prevQual
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
name|prevQual
operator|.
name|getBytes
argument_list|()
argument_list|,
name|prevQual
operator|.
name|getOffset
argument_list|()
argument_list|,
name|prevQual
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
name|prevQual
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
name|currentVersions
operator|=
literal|0
expr_stmt|;
block|}
name|currentVersions
operator|++
expr_stmt|;
if|if
condition|(
name|currentVersions
operator|>
name|familyMaxVersions
condition|)
block|{
return|return
name|ReturnCode
operator|.
name|SKIP
return|;
block|}
if|if
condition|(
name|authManager
operator|.
name|authorize
argument_list|(
name|user
argument_list|,
name|table
argument_list|,
name|cell
argument_list|,
name|cellFirstStrategy
argument_list|,
name|Permission
operator|.
name|Action
operator|.
name|READ
argument_list|)
condition|)
block|{
return|return
name|ReturnCode
operator|.
name|INCLUDE
return|;
block|}
comment|// Before per cell ACLs we used to return the NEXT_COL hint, but we can
comment|// no longer do that since, given the possibility of per cell ACLs
comment|// anywhere, we now need to examine all KVs with this filter.
return|return
name|ReturnCode
operator|.
name|SKIP
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
name|prevFam
operator|.
name|unset
argument_list|()
expr_stmt|;
name|this
operator|.
name|prevQual
operator|.
name|unset
argument_list|()
expr_stmt|;
name|this
operator|.
name|familyMaxVersions
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|currentVersions
operator|=
literal|0
expr_stmt|;
block|}
comment|/**    * @return The filter serialized using pb    */
specifier|public
name|byte
index|[]
name|toByteArray
parameter_list|()
block|{
comment|// no implementation, server-side use only
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Serialization not supported.  Intended for server-side use only."
argument_list|)
throw|;
block|}
comment|/**    * @param pbBytes A pb serialized {@link AccessControlFilter} instance    * @return An instance of {@link AccessControlFilter} made from<code>bytes</code>    * @throws org.apache.hadoop.hbase.exceptions.DeserializationException    * @see {@link #toByteArray()}    */
specifier|public
specifier|static
name|AccessControlFilter
name|parseFrom
parameter_list|(
specifier|final
name|byte
index|[]
name|pbBytes
parameter_list|)
throws|throws
name|DeserializationException
block|{
comment|// no implementation, server-side use only
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Serialization not supported.  Intended for server-side use only."
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

