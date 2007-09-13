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
name|shell
package|;
end_package

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

begin_class
specifier|public
class|class
name|HelpContents
block|{
comment|/**    * add help contents     */
specifier|public
specifier|static
name|Map
operator|<
condition|?
then|extends
name|String
operator|,
operator|?
expr|extends
name|String
index|[]
operator|>
name|Load
argument_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
index|[]
argument_list|>
name|load
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
index|[]
argument_list|>
argument_list|()
block|;
name|String
name|columnName
operator|=
literal|"column_name: "
operator|+
literal|"\n\t  column_family_name"
operator|+
literal|"\n\t| column_family_name:column_label_name"
block|;
name|String
name|columnList
operator|=
literal|"{column_name, [, column_name] ... | *}"
block|;
name|load
operator|.
name|put
argument_list|(
literal|"SHOW"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"List all available tables"
block|,
literal|"SHOW TABLES;"
block|}
argument_list|)
block|;
name|load
operator|.
name|put
argument_list|(
literal|"FS"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"Hadoop FsShell operations."
block|,
literal|"FS -copyFromLocal /home/user/backup.dat fs/user/backup;"
block|}
argument_list|)
block|;
name|load
operator|.
name|put
argument_list|(
literal|"CLEAR"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"Clear the screen"
block|,
literal|"CLEAR;"
block|}
argument_list|)
block|;
name|load
operator|.
name|put
argument_list|(
literal|"DESCRIBE"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"Print information about tables"
block|,
literal|"[DESCRIBE|DESC] table_name;"
block|}
argument_list|)
block|;
name|load
operator|.
name|put
argument_list|(
literal|"CREATE"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"Create tables"
block|,
literal|"CREATE TABLE table_name"
operator|+
literal|"\n\t(column_family_spec [, column_family_spec] ...);"
operator|+
literal|"\n\n"
operator|+
literal|"column_family_spec:"
operator|+
literal|"\n\tcolumn_family_name"
operator|+
literal|"\n\t[MAX_VERSIONS=n]"
operator|+
literal|"\n\t[MAX_LENGTH=n]"
operator|+
literal|"\n\t[COMPRESSION=NONE|RECORD|BLOCK]"
operator|+
literal|"\n\t[IN_MEMORY]"
operator|+
literal|"\n\t[BLOOMFILTER=NONE|BLOOM|COUNTING|RETOUCHED VECTOR_SIZE=n NUM_HASH=n]"
block|}
argument_list|)
block|;
name|load
operator|.
name|put
argument_list|(
literal|"DROP"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"Drop tables"
block|,
literal|"DROP TABLE table_name [, table_name] ...;"
block|}
argument_list|)
block|;
name|load
operator|.
name|put
argument_list|(
literal|"INSERT"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"Insert values into tables"
block|,
literal|"INSERT INTO table_name"
operator|+
literal|"\n\t(column_name, ...) VALUES ('value', ...)"
operator|+
literal|"\n\tWHERE row='row_key';"
operator|+
literal|"\n\n"
operator|+
name|columnName
block|}
argument_list|)
block|;
name|load
operator|.
name|put
argument_list|(
literal|"DELETE"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"Delete a subset of the data in a table"
block|,
literal|"DELETE "
operator|+
name|columnList
operator|+
literal|"\n\tFROM table_name"
operator|+
literal|"\n\tWHERE row='row-key';"
operator|+
literal|"\n\n"
operator|+
name|columnName
block|}
argument_list|)
block|;
name|load
operator|.
name|put
argument_list|(
literal|"SELECT"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"Select values from tables"
block|,
literal|"SELECT "
operator|+
name|columnList
operator|+
literal|" FROM table_name"
operator|+
literal|"\n\t[WHERE row='row_key' | STARTING FROM 'row-key']"
operator|+
literal|"\n\t[NUM_VERSIONS = version_count]"
operator|+
literal|"\n\t[TIMESTAMP 'timestamp']"
operator|+
literal|"\n\t[LIMIT = row_count]"
operator|+
literal|"\n\t[INTO FILE 'file_name'];"
block|}
argument_list|)
block|;
name|load
operator|.
name|put
argument_list|(
literal|"ALTER"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"Alter the structure of a table"
block|,
literal|"ALTER TABLE table_name"
operator|+
literal|"\n\t  ADD column_spec"
operator|+
literal|"\n\t| ADD (column_spec, column_spec, ...)"
operator|+
literal|"\n\t| DROP column_family_name"
operator|+
literal|"\n\t| CHANGE column_spec;"
block|}
argument_list|)
block|;
name|load
operator|.
name|put
argument_list|(
literal|"EXIT"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"Exit shell"
block|,
literal|"EXIT;"
block|}
argument_list|)
block|;
return|return
name|load
return|;
block|}
block|}
end_class

end_unit

