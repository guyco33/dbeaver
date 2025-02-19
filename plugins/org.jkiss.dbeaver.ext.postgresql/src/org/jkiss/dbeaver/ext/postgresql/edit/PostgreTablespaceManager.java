/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2019 Serge Rider (serge@jkiss.org)
 * Copyright (C) 2019 Andrew Khitrin (ahitrin@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jkiss.dbeaver.ext.postgresql.edit;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.ext.postgresql.model.PostgreDatabase;
import org.jkiss.dbeaver.ext.postgresql.model.PostgreTablespace;
import org.jkiss.dbeaver.ext.postgresql.model.generic.PostgreMetaModel;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.edit.DBECommandContext;
import org.jkiss.dbeaver.model.edit.DBEPersistAction;
import org.jkiss.dbeaver.model.impl.DBSObjectCache;
import org.jkiss.dbeaver.model.impl.edit.SQLDatabasePersistActionAtomic;
import org.jkiss.dbeaver.model.impl.sql.edit.SQLObjectEditor;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;


public class PostgreTablespaceManager extends SQLObjectEditor<PostgreTablespace, PostgreDatabase>{
    
    private final static Set<String> systemTablespaces = new HashSet<>(Arrays.asList("pg_default", "pg_global"));
    
    @Override
    public void deleteObject(DBECommandContext commandContext, PostgreTablespace object, Map<String, Object> options)
            throws DBException {
         if (systemTablespaces.contains(object.getName().toLowerCase())) {
             MessageDialog.openInformation(null, "Drop tablespace",
                     String.format("Unable to drop system tablespace %s", object.getName()));
  
         } else { 
           super.deleteObject(commandContext, object, options);
         }
    }

    private static final Log log = Log.getLog(PostgreTablespaceManager.class);
    
    @Override
    public long getMakerOptions(DBPDataSource dataSource) {
        return FEATURE_SAVE_IMMEDIATELY;
    }

    @Override
    public DBSObjectCache<PostgreDatabase, PostgreTablespace> getObjectsCache(PostgreTablespace object) {
        return object.getDatabase().tablespaceCache;
    }

    @Override
    protected PostgreTablespace createDatabaseObject(DBRProgressMonitor monitor, DBECommandContext context,
            Object container, Object copyFrom, Map<String, Object> options) throws DBException {
        return new PostgreTablespace((PostgreDatabase) container);
    }


    @Override
    protected void addObjectCreateActions(DBRProgressMonitor monitor, List<DBEPersistAction> actions,
            SQLObjectEditor<PostgreTablespace, PostgreDatabase>.ObjectCreateCommand command,
            Map<String, Object> options) {
        final PostgreTablespace tablespace = command.getObject();

        try {
            actions.add(
                new SQLDatabasePersistActionAtomic("Create tablespace",tablespace.getObjectDefinitionText(monitor, options)) //$NON-NLS-2$
            );
        } catch (DBException e) {
           log.error(e);
        }
    }

    @Override
    protected void addObjectDeleteActions(List<DBEPersistAction> actions,
            SQLObjectEditor<PostgreTablespace, PostgreDatabase>.ObjectDeleteCommand command,
            Map<String, Object> options) {
        

                actions.add(
                        new SQLDatabasePersistActionAtomic("Drop tablespace", "DROP TABLESPACE " + command.getObject().getName()) //$NON-NLS-2$
                    );
        
     }

    @Override
    public boolean canCreateObject(Object container) {
         return true;
    }

    @Override
    public boolean canDeleteObject(PostgreTablespace object) {
        return true;
    }

    @Override
    public boolean canEditObject(PostgreTablespace object) {
        return false;
    }
}
