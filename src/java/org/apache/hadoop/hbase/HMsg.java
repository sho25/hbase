begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2006-7 The Apache Software Foundation  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|org
operator|.
name|apache
operator|.
name|hadoop
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
name|io
operator|.
name|*
import|;
end_import

begin_comment
comment|/*******************************************************************************  * HMsg is for communicating instructions between the HMaster and the   * HRegionServers.  ******************************************************************************/
end_comment

begin_class
specifier|public
class|class
name|HMsg
implements|implements
name|Writable
block|{
specifier|public
specifier|static
specifier|final
name|byte
name|MSG_REGION_OPEN
init|=
literal|1
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
name|MSG_REGION_CLOSE
init|=
literal|2
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
name|MSG_REGION_MERGE
init|=
literal|3
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
name|MSG_CALL_SERVER_STARTUP
init|=
literal|4
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
name|MSG_REGIONSERVER_ALREADY_RUNNING
init|=
literal|5
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
name|MSG_REGION_CLOSE_WITHOUT_REPORT
init|=
literal|6
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
name|MSG_REGION_CLOSE_AND_DELETE
init|=
literal|7
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
name|MSG_REPORT_OPEN
init|=
literal|100
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
name|MSG_REPORT_CLOSE
init|=
literal|101
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
name|MSG_REGION_SPLIT
init|=
literal|102
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
name|MSG_NEW_REGION
init|=
literal|103
decl_stmt|;
name|byte
name|msg
decl_stmt|;
name|HRegionInfo
name|info
decl_stmt|;
specifier|public
name|HMsg
parameter_list|()
block|{
name|this
operator|.
name|info
operator|=
operator|new
name|HRegionInfo
argument_list|()
expr_stmt|;
block|}
specifier|public
name|HMsg
parameter_list|(
name|byte
name|msg
parameter_list|)
block|{
name|this
operator|.
name|msg
operator|=
name|msg
expr_stmt|;
name|this
operator|.
name|info
operator|=
operator|new
name|HRegionInfo
argument_list|()
expr_stmt|;
block|}
specifier|public
name|HMsg
parameter_list|(
name|byte
name|msg
parameter_list|,
name|HRegionInfo
name|info
parameter_list|)
block|{
name|this
operator|.
name|msg
operator|=
name|msg
expr_stmt|;
name|this
operator|.
name|info
operator|=
name|info
expr_stmt|;
block|}
specifier|public
name|byte
name|getMsg
parameter_list|()
block|{
return|return
name|msg
return|;
block|}
specifier|public
name|HRegionInfo
name|getRegionInfo
parameter_list|()
block|{
return|return
name|info
return|;
block|}
comment|//////////////////////////////////////////////////////////////////////////////
comment|// Writable
comment|//////////////////////////////////////////////////////////////////////////////
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
name|out
operator|.
name|writeByte
argument_list|(
name|msg
argument_list|)
expr_stmt|;
name|info
operator|.
name|write
argument_list|(
name|out
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
name|msg
operator|=
name|in
operator|.
name|readByte
argument_list|()
expr_stmt|;
name|this
operator|.
name|info
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

