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
operator|.
name|hfile
operator|.
name|bucket
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
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
name|security
operator|.
name|MessageDigest
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|NoSuchAlgorithmException
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
name|util
operator|.
name|Shell
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

begin_comment
comment|/**  * A class implementing PersistentIOEngine interface supports file integrity verification  * for {@link BucketCache} which use persistent IOEngine  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|PersistentIOEngine
implements|implements
name|IOEngine
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|PersistentIOEngine
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|DuFileCommand
name|DU
init|=
operator|new
name|DuFileCommand
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"du"
block|,
literal|""
block|}
argument_list|)
decl_stmt|;
specifier|protected
specifier|final
name|String
index|[]
name|filePaths
decl_stmt|;
specifier|public
name|PersistentIOEngine
parameter_list|(
name|String
modifier|...
name|filePaths
parameter_list|)
block|{
name|this
operator|.
name|filePaths
operator|=
name|filePaths
expr_stmt|;
block|}
comment|/**    * Verify cache files's integrity    * @param algorithm the backingMap persistence path    */
specifier|protected
name|void
name|verifyFileIntegrity
parameter_list|(
name|byte
index|[]
name|persistentChecksum
parameter_list|,
name|String
name|algorithm
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
index|[]
name|calculateChecksum
init|=
name|calculateChecksum
argument_list|(
name|algorithm
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|Bytes
operator|.
name|equals
argument_list|(
name|persistentChecksum
argument_list|,
name|calculateChecksum
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Mismatch of checksum! The persistent checksum is "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|persistentChecksum
argument_list|)
operator|+
literal|", but the calculate checksum is "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|calculateChecksum
argument_list|)
argument_list|)
throw|;
block|}
block|}
comment|/**    * Using an encryption algorithm to calculate a checksum, the default encryption algorithm is MD5    * @return the checksum which is convert to HexString    * @throws IOException something happened like file not exists    * @throws NoSuchAlgorithmException no such algorithm    */
specifier|protected
name|byte
index|[]
name|calculateChecksum
parameter_list|(
name|String
name|algorithm
parameter_list|)
block|{
try|try
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|filePath
range|:
name|filePaths
control|)
block|{
name|File
name|file
init|=
operator|new
name|File
argument_list|(
name|filePath
argument_list|)
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|filePath
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|getFileSize
argument_list|(
name|filePath
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|file
operator|.
name|lastModified
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|MessageDigest
name|messageDigest
init|=
name|MessageDigest
operator|.
name|getInstance
argument_list|(
name|algorithm
argument_list|)
decl_stmt|;
name|messageDigest
operator|.
name|update
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|messageDigest
operator|.
name|digest
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioex
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Calculating checksum failed, because of "
argument_list|,
name|ioex
argument_list|)
expr_stmt|;
return|return
operator|new
name|byte
index|[
literal|0
index|]
return|;
block|}
catch|catch
parameter_list|(
name|NoSuchAlgorithmException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"No such algorithm : "
operator|+
name|algorithm
operator|+
literal|"!"
argument_list|)
expr_stmt|;
return|return
operator|new
name|byte
index|[
literal|0
index|]
return|;
block|}
block|}
comment|/**    * Using Linux command du to get file's real size    * @param filePath the file    * @return file's real size    * @throws IOException something happened like file not exists    */
specifier|private
specifier|static
name|long
name|getFileSize
parameter_list|(
name|String
name|filePath
parameter_list|)
throws|throws
name|IOException
block|{
name|DU
operator|.
name|setExecCommand
argument_list|(
name|filePath
argument_list|)
expr_stmt|;
name|DU
operator|.
name|execute
argument_list|()
expr_stmt|;
return|return
name|Long
operator|.
name|parseLong
argument_list|(
name|DU
operator|.
name|getOutput
argument_list|()
operator|.
name|split
argument_list|(
literal|"\t"
argument_list|)
index|[
literal|0
index|]
argument_list|)
return|;
block|}
specifier|private
specifier|static
class|class
name|DuFileCommand
extends|extends
name|Shell
operator|.
name|ShellCommandExecutor
block|{
specifier|private
name|String
index|[]
name|execCommand
decl_stmt|;
name|DuFileCommand
parameter_list|(
name|String
index|[]
name|execString
parameter_list|)
block|{
name|super
argument_list|(
name|execString
argument_list|)
expr_stmt|;
name|execCommand
operator|=
name|execString
expr_stmt|;
block|}
name|void
name|setExecCommand
parameter_list|(
name|String
name|filePath
parameter_list|)
block|{
name|this
operator|.
name|execCommand
index|[
literal|1
index|]
operator|=
name|filePath
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
index|[]
name|getExecString
parameter_list|()
block|{
return|return
name|this
operator|.
name|execCommand
return|;
block|}
block|}
block|}
end_class

end_unit

